from django.http.response import Http404, HttpResponse, HttpResponseBadRequest
import json
from busfeedback.utilities.bus_updater import update_services_and_stops, delete_services_stops
from busfeedback.models.service import Service
from busfeedback.models.journey import Journey
from busfeedback.models.stop import Stop
from busfeedback.serializers.service_serializer import ServiceSerializer
from busfeedback.serializers.stop_serializer import StopSerializer
from rest_framework.renderers import JSONRenderer
from django.core.exceptions import ObjectDoesNotExist
from django.utils import timezone
import dateutil.parser
from django.db import transaction


def get_data(request):
    update_services_and_stops()

    return HttpResponse("Done", content_type='application/json')


def remove_data(request):
    delete_services_stops()

    return HttpResponse("Done", content_type='application/json')


def get_services_for_stop(request):
    stop_id = int(request.POST.get("stop_id", ""))
    stop = Stop.objects.filter(stop_id=stop_id)[0]
    services = list(stop.services.all())
    service_serializer = ServiceSerializer(services, many=True)
    json_services = JSONRenderer().render(service_serializer.data)

    return HttpResponse(json_services, content_type='application/json')


def get_closest_stops(request):
    user_latitude = request.POST.get("latitude", "")
    user_longitude = request.POST.get("longitude", "")
    number_of_stops = int(request.POST.get("number_of_stops", ""))

    closest_stops = Stop.objects.get_closest_stops(latitude=user_latitude, longitude=user_longitude,number_of_stops=number_of_stops)
    stop_serializer = StopSerializer(closest_stops, many=True)
    json_stops = JSONRenderer().render(stop_serializer.data)

    return HttpResponse(json_stops, content_type='application/json')


def upload_new_trip(request):
    journey_id = request.POST.get("journey_id", None)
    new_trip = request.POST.get("trip", None)

    if not new_trip:
        error_message = "No trip was provided as part of request parameters."
        return HttpResponseBadRequest(error_message, content_type='application/json')

    new_trip = json.loads(new_trip)

    try:
        start_stop = Stop.objects.get(id=new_trip["start_stop"])
    except ObjectDoesNotExist:
        error_message = "No existing stop could be found with ID {}".format(new_trip["start_stop"])
        return HttpResponseBadRequest(error_message, content_type='application/json')

    try:
        end_stop = Stop.objects.get(id=new_trip["end_stop"])
    except ObjectDoesNotExist:
        error_message = "No existing stop could be found with ID {}".format(new_trip["end_stop"])
        return HttpResponseBadRequest(error_message, content_type='application/json')

    try:
        service = Service.objects.get(id=new_trip["service"])
    except ObjectDoesNotExist:
        error_message = "No existing stop could be found with ID {}".format(id=new_trip["service"])
        return HttpResponseBadRequest(error_message, content_type='application/json')

    start_time = timezone.make_aware(dateutil.parser.parse(new_trip["start_time"]), timezone.get_current_timezone())
    end_time = timezone.make_aware(dateutil.parser.parse(new_trip["end_time"]), timezone.get_current_timezone())

    # Get existing or create a new journey
    if journey_id:
        journey_id = int(journey_id)
        try:
            journey = Journey.objects.get(id=journey_id)
            journey.end_time = end_time
            journey.save()
        except ObjectDoesNotExist:
            error_message = "No existing journey could be found with ID {}".format(journey_id)
            return HttpResponseBadRequest(error_message, content_type='application/json')
    else:
        journey = Journey.objects.create(start_time=start_time, end_time=end_time)

    # Create a new trip for a journey
    with transaction.atomic():
        trip = journey.trips.create(
            start_time=start_time,
            end_time=end_time,
            start_stop=start_stop,
            end_stop=end_stop,
            service=service,
            wait_duration=new_trip["wait_duration"],
            travel_duration=new_trip["travel_duration"],
            seat=new_trip["seat"],
            rating=new_trip["rating"]
        )

    response_date = json.dumps({"journey_id": journey.id})

    return HttpResponse(response_date, content_type='application/json')


