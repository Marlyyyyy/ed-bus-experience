from busfeedback.models.update import Update
from busfeedback.models.service import Service, ServiceStop
from busfeedback.models.stop import Stop
import requests
from busfeedback.constants import *
import json
from datetime import datetime
from django.utils import timezone
from django.db import transaction
from django.db import IntegrityError


def is_table_filled(table_name):
    return Update.objects.filter(table_name=table_name).exists()


# Updates the date corresponding to when a specific table was last updated
def update_table_date(table_name, new_date):
    if is_table_filled(table_name):
        update = Update.objects.filter(table_name=table_name)[0]
        update.last_updated = new_date
    else:
        update = Update(table_name=table_name, last_updated=new_date)

    update.save()


# Returns True if the selected table requires to be updated. Parameter "difference" has to be milliseconds.
def check_if_need_update(table_name, current_date, difference):
    if is_table_filled(table_name):
        update = Update.objects.filter(table_name=table_name)[0]
        # last_updated = timezone.make_aware(update.last_updated, timezone.get_current_timezone())
        delta = current_date - update.last_updated
        actual_difference = int(delta.total_seconds() * 1000)
        if actual_difference > difference:
            return True
        else:
            return False
    else:
        return True


# Deletes existing data from stops, services and their linking table
def delete_services_stops():
    Service.objects.delete_everything()
    Stop.objects.delete_everything()


# Fills up both services and stops with up-to-date data
@transaction.atomic
def update_services_and_stops():

    # (Datetime of last update, List of objects) tuples
    service_date, services = get_external_list_of_services()
    stop_date, stops = get_external_list_of_stops()

    update_table_date("service", service_date)
    update_table_date("stop", stop_date)

    insert_or_update(services, stops)


# Modifies the database using the provided list of service and stop objects
def insert_or_update(list_of_services, list_of_stops):

    existing_stops = Stop.objects.all()
    existing_services = Service.objects.all()

    # If both tables are empty, insert all data. Otherwise update existing data, and insert new data
    if not existing_stops and not existing_services:
        create_and_insert_new_services_from_dictionary(list_of_services)
        create_and_insert_new_stops_from_dictionary(list_of_stops)

    else:
        # Create dictionary of key: service_name, value: service
        new_services = {}
        for service in list_of_services:
            new_services[service["name"]] = service

        # Create dictionary of key: stop_id, value: stop
        new_stops = {}
        for stop in list_of_stops:
            new_stops[stop["stop_id"]] = stop

        # Update existing stops
        for stop in existing_stops:
            # stop.services.clear()
            existing_stop = new_stops.pop(stop.stop_id, None)

            if existing_stop:
                stop.stop_id = existing_stop["stop_id"]
                stop.name = existing_stop["name"]
                stop.latitude = existing_stop["latitude"]
                stop.longitude = existing_stop["longitude"]
                stop.orientation = existing_stop["orientation"]
                # stop.save()

        # Update existing
        Stop.objects.bulk_update(existing_stops, batch_size=50)

        for service in existing_services:
            existing_service = new_services.pop(service.name, None)
            if existing_service:
                service.name = existing_service["name"]
                service.type = existing_service["service_type"]
                service.description = existing_service["description"]
                # service.save()

        # Update existing
        Service.objects.bulk_update(existing_services, batch_size=50) # TODO: use with batch_size=900 parameter to make this usable with SQLite

        # Insert new
        create_and_insert_new_stops_from_dictionary(new_stops.values())
        create_and_insert_new_services_from_dictionary(new_services.values())

    # Manage the relationship between Service and Stop
    saved_stops = list(Stop.objects.all())
    saved_services = list(Service.objects.all())

    saved_stops_dictionary = {}
    for stop in saved_stops:
        saved_stops_dictionary[stop.stop_id] = stop

    # Create a dictionary of key: service name, value: [(direction, stop_id's in order)] list
    ordered_stops_per_service_id = {}
    for service in list_of_services:
        service_name = service["name"]
        ordered_stops_per_service_id[service_name] = []
        direction = 0
        inbound_found = False
        outbound_found = False
        for route in service["routes"]:
            if route["direction"] == "inbound":
                if inbound_found:
                    continue
                inbound_found = True
            elif route["direction"] == "outbound":
                if outbound_found:
                    continue
                outbound_found = True
            else:
                raise Exception("Invalid direction found for service " + service_name)

            ordered_stops_per_service_id[service_name].append((str(direction), route["stops"]))
            direction += 1

            # Ad-hoc fix, because there's no need for a third direction
            if inbound_found and outbound_found:
                break

    # Add stops to services
    new_service_stops = []
    for service in saved_services:
        direction_stop_id_tuples = ordered_stops_per_service_id[service.name]
        for direction_stop_id_tuple in direction_stop_id_tuples:
            direction = direction_stop_id_tuple[0]
            stop_ids = direction_stop_id_tuple[1]
            order = 0
            for stop_id in stop_ids:
                stop = saved_stops_dictionary[stop_id]

                # Create the association
                try:
                    new_service_stops.append(ServiceStop(service=service, stop=stop, direction=int(direction), order=order))
                except IntegrityError:
                    break
                order += 1

    ServiceStop.objects.all().delete()
    ServiceStop.objects.bulk_create(new_service_stops, batch_size=50)


# Gets the last updated time of services, and the list of service objects
def get_external_list_of_services():

    # Get services as json data
    services_json = requests.get(API_SERVICES, headers=API_HEADER)
    services_dictionary = json.loads(services_json.text)
    service_date = timezone.make_aware(datetime.utcfromtimestamp(int(services_dictionary["last_updated"])), timezone.get_current_timezone())

    return service_date, services_dictionary["services"]


# Gets the last updated time of stops, and the list of stop objects
def get_external_list_of_stops():

    # Get stops as json data
    stops_json = requests.get(API_STOPS, headers=API_HEADER)
    stops_dictionary = json.loads(stops_json.text)
    stop_date = timezone.make_aware(datetime.utcfromtimestamp(int(stops_dictionary["last_updated"])), timezone.get_current_timezone())

    return stop_date, stops_dictionary["stops"]


# Creates a list of service objects to be inserted into the database
def create_and_insert_new_services_from_dictionary(services):

    new_services = []
    for service in services:
        new_services.append(Service(name=service["name"], type=service["service_type"], description=service["description"]))

    Service.objects.bulk_create(new_services, batch_size=50)


# Creates a list of stop objects to be inserted into the database
def create_and_insert_new_stops_from_dictionary(stops):

    new_stops = []
    for stop in stops:
        new_stops.append(Stop(stop_id=stop["stop_id"],
                              name=stop["name"],
                              latitude=stop["latitude"],
                              longitude=stop["longitude"],
                              orientation=stop["orientation"]))

    Stop.objects.bulk_create(new_stops, batch_size=50)