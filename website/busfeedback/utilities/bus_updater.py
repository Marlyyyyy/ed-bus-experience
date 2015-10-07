from busfeedback.models.update import Update
from busfeedback.models.service import Service
from busfeedback.models.stop import Stop
import requests
from busfeedback.constants import *
import json
from datetime import datetime
from django.utils import timezone
from django.db import transaction


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

    # Create a dictionary of key: stop_id, value: service names
    stop_services_dictionary = {}
    for stop in stops:
        stop_services_dictionary[stop["stop_id"]] = set(stop["services"])

    existing_stops = Stop.objects.all()
    existing_services = Service.objects.all()

    # If both tables are empty, insert all data. Otherwise update existing data, and insert new data
    if not existing_stops and not existing_services:
        create_and_insert_new_services_from_dictionary(services)
        create_and_insert_new_stops_from_dictionary(stops)

    else:
        # Create dictionary of key: service_name, value: service
        new_services = {}
        for service in services:
            new_services[service["name"]] = service

        # Create dictionary of key: stop_id, value: stop
        new_stops = {}
        for stop in stops:
            new_stops[stop["stop_id"]] = stop

        for stop in existing_stops:
            stop.services.clear()
            new_stop = new_stops.pop(stop.stop_id, None)

            # If it's a new stop, we'll persist it later
            if new_stop:
                stop.stop_id = new_stop["stop_id"]
                stop.latitude = new_stop["latitude"]
                stop.longitude = new_stop["longitude"]

        for service in existing_services:
            new_service = new_services.pop(service.name, None)
            if new_service:
                service.name = new_service["name"]
                service.type = new_service["service_type"]
                service.description = new_service["description"]

        # Update existing
        Stop.objects.bulk_update(existing_stops)
        Service.objects.bulk_update(existing_services)

        # Insert new
        create_and_insert_new_stops_from_dictionary(new_stops.values())
        create_and_insert_new_services_from_dictionary(new_services.values())

    # Manage the relationship between Service and Stop
    saved_stops = list(Stop.objects.all())
    saved_services = list(Service.objects.all())

    saved_services_dictionary = {}
    for service in saved_services:
        saved_services_dictionary[service.name] = service

    # Add services to stops
    for stop in saved_stops:
        service_names_to_add = stop_services_dictionary.get(stop.stop_id)
        if service_names_to_add:
            services_to_add = []
            for service_name in service_names_to_add:
                services_to_add.append(saved_services_dictionary[service_name])
            stop.services.add(*services_to_add)


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


def create_and_insert_new_services_from_dictionary(services):
    # Create a list of service objects to be inserted into the database
    new_services = []
    for service in services:
        new_services.append(Service(name=service["name"], type=service["service_type"], description=service["description"]))

    Service.objects.bulk_create(new_services)


def create_and_insert_new_stops_from_dictionary(stops):
    # Create a list of stop objects to be inserted into the database
    new_stops = []
    for stop in stops:
        new_stops.append(Stop(stop_id=stop["stop_id"], latitude=stop["latitude"], longitude=stop["longitude"]))

    Stop.objects.bulk_create(new_stops)