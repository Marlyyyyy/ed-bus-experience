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

    delete_services_stops()

    # Get services as json data
    services_json = requests.get(API_SERVICES, headers=API_HEADER)
    services_dictionary = json.loads(services_json.text)

    # Array of service objects
    services = services_dictionary["services"]

    # Get stops as json data
    stops_json = requests.get(API_STOPS, headers=API_HEADER)
    stops_dictionary = json.loads(stops_json.text)

    # Array of stop objects
    stops = stops_dictionary["stops"]

    # Update the tables' last_updated field
    service_date = timezone.make_aware(datetime.utcfromtimestamp(int(services_dictionary["last_updated"])), timezone.get_current_timezone())
    update_table_date("service", service_date)
    stop_date = timezone.make_aware(datetime.utcfromtimestamp(int(stops_dictionary["last_updated"])), timezone.get_current_timezone())
    update_table_date("stop", stop_date)

    # Create a dictionary of stopID,Services
    stop_services_dictionary = {}
    for stop in stops:
        # Must take the set because sometimes the service names are listed multiple times for a particular stop.
        service_names = set(stop["services"])
        stop_services_dictionary[stop["stop_id"]] = service_names

    # Create a service instance of each service
    new_services = []
    for service in services:
        new_services.append(Service(name=service["name"], type=service["service_type"], description=service["description"]))

    # Create a stop instance of each stop and add corresponding services to them
    new_stops = []
    for stop in stops:
        new_stops.append(Stop(name=stop["stop_id"], latitude=stop["latitude"], longitude=stop["longitude"]))

    # Insert all services using a single query
    Service.objects.bulk_create(new_services)

    # Insert all stops using a single query
    Stop.objects.bulk_create(new_stops)

    # Manage the relationship between Service and Stop
    saved_stops = list(Stop.objects.all())
    saved_services = list(Service.objects.all())

    saved_services_dictionary = {}
    for service in saved_services:
        saved_services_dictionary[service.name] = service

    # Add services to stops
    for stop in saved_stops:
        service_names_to_add = stop_services_dictionary[stop.name]
        services_to_add = []
        for service_name in service_names_to_add:
            services_to_add.append(saved_services_dictionary[service_name])
        stop.services.add(*services_to_add)



