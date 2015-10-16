from django.http.response import Http404, HttpResponse, HttpResponseBadRequest
import json
from busfeedback.utilities.bus_updater import update_services_and_stops, delete_services_stops
from busfeedback.models.service import Service
from busfeedback.models.journey import Journey
from busfeedback.models.stop import Stop
from django.contrib.auth.models import User
from busfeedback.serializers.service_serializer import ServiceSerializer
from busfeedback.serializers.stop_serializer import StopSerializer
from busfeedback.serializers.journey_serializer import JourneySerializer
from rest_framework.renderers import JSONRenderer
from django.core.exceptions import ObjectDoesNotExist
from django.utils import timezone
import dateutil.parser
from django.db import transaction
from django.views.decorators.csrf import csrf_exempt


@csrf_exempt
def get_data(request):
    update_services_and_stops()

    return HttpResponse("{'data': 'Done'}", content_type='application/json')


@csrf_exempt
def remove_data(request):
    delete_services_stops()

    return HttpResponse("Done", content_type='application/json')


@csrf_exempt
def get_services_for_stop(request):
    stop_id = int(request.GET.get("stop_id", ""))
    # TODO: Handle exception
    stop = Stop.objects.filter(stop_id=stop_id)[0]
    services = list(stop.services.all())
    service_serializer = ServiceSerializer(services, many=True)
    json_services = JSONRenderer().render({"services": service_serializer.data})

    return HttpResponse(json_services, content_type='application/json')


@csrf_exempt
def get_closest_stops(request):
    user_latitude = request.GET.get("latitude", "")
    user_longitude = request.GET.get("longitude", "")
    number_of_stops = int(request.GET.get("number_of_stops", ""))

    closest_stops = Stop.objects.get_closest_stops(latitude=user_latitude, longitude=user_longitude,number_of_stops=number_of_stops)
    stop_serializer = StopSerializer(closest_stops, many=True)
    json_stops = JSONRenderer().render({"stops": stop_serializer.data})

    return HttpResponse(json_stops, content_type='application/json')


@csrf_exempt
def upload_new_trip(request):
    journey_id = request.POST.get("journey_id", None)
    new_trip = request.POST.get("trip", None)

    if not new_trip:
        error_message = "No trip was provided as part of request parameters."
        return HttpResponseBadRequest(error_message, content_type='application/json')

    new_trip = json.loads(new_trip)

    # Check if Start Stop exists
    start_stop = Stop.objects.get_or_none(id=new_trip["start_stop_id"])
    if not start_stop:
        error_message = "No existing stop could be found with ID {}".format(new_trip["start_stop_id"])
        return HttpResponseBadRequest(error_message, content_type='application/json')

    # Check if End Stop exists
    end_stop = Stop.objects.get_or_none(id=new_trip["end_stop_id"])
    if not end_stop:
        error_message = "No existing stop could be found with ID {}".format(new_trip["end_stop_id"])
        return HttpResponseBadRequest(error_message, content_type='application/json')

    # Check if Service exists
    service = Service.objects.get_or_none(id=new_trip["service_id"])
    if not service:
        error_message = "No existing service could be found with ID {}".format(new_trip["service_id"])
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

        # Add journey to the user's diary if logged in
        if request.user.is_authenticated():
            journey.account = request.user
            journey.save()

    # Create a new trip for a journey
    with transaction.atomic():
        journey.trips.create(
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

    response_data = json.dumps({"journey_id": journey.id})

    return HttpResponse(response_data, content_type='application/json')


@csrf_exempt
def get_diary_for_user(request):
    username = request.GET.get("username", "")

    try:
        journeys = Journey.objects.filter(account__username=username).prefetch_related('trips')
    except ObjectDoesNotExist:
        error_message = "User with username '{}' could not be found.".format(username)
        return HttpResponseBadRequest(error_message, content_type='application/json')

    journey_serliazer = JourneySerializer(journeys, many=True)
    json_journeys = JSONRenderer().render({"journeys": journey_serliazer.data})

    return HttpResponse(json_journeys, content_type='application/json')



