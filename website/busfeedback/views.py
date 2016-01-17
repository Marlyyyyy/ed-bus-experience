from django.http.response import Http404, HttpResponse, HttpResponseBadRequest
import json
from django.http import HttpResponse, HttpResponseNotFound
from busfeedback.utilities.bus_updater import update_services_and_stops, delete_services_stops
from busfeedback.models.service import Service, ServiceStop
from busfeedback.models.journey import Journey
from busfeedback.models.stop import Stop
from busfeedback.models.questionnaire import Questionnaire
from rest_framework.views import APIView
from rest_framework import permissions
from rest_framework_jwt.authentication import JSONWebTokenAuthentication
from busfeedback.serializers.service_serializer import ServiceSerializer
from busfeedback.serializers.stop_serializer import StopSerializer
from busfeedback.serializers.journey_serializer import JourneySerializer
from rest_framework.renderers import JSONRenderer
from django.core.exceptions import ObjectDoesNotExist
from django.utils import timezone
import dateutil.parser
from django.db import transaction
from django.views.decorators.csrf import csrf_exempt
import math
from django.template import loader


@csrf_exempt
def get_data(request):
    # update_services_and_stops()

    # services_json = requests.get(API_SERVICES, headers=API_HEADER)
    return HttpResponse("lol", content_type='application/json')


@csrf_exempt
def remove_data(request):
    delete_services_stops()

    return HttpResponse("Done", content_type='application/json')

@csrf_exempt
def custom_404(request):
    return HttpResponseNotFound('<h1>Page not found</h1>')


class ServicesForStopView(APIView):

    permission_classes = (permissions.IsAuthenticated, )
    authentication_classes = (JSONWebTokenAuthentication, )

    def get(self, request):
        stop_id = int(request.GET.get("id", ""))
        # TODO: Handle exception
        stop = Stop.objects.filter(id=stop_id)[0]
        services = list(stop.services.all())
        service_serializer = ServiceSerializer(services, many=True)
        json_services = JSONRenderer().render({"services": service_serializer.data})

        return HttpResponse(json_services, content_type='application/json')


class StopsForServiceView(APIView):

    permission_classes = (permissions.IsAuthenticated, )
    authentication_classes = (JSONWebTokenAuthentication, )

    def get(self, request):
        service_id = int(request.GET.get("service_id", ""))
        start_stop_id = request.GET.get("start_stop_id", "")
        # TODO: Handle exception

        # If there's a start_stop, then filter end_stops based on start_stop and service
        if start_stop_id != "":
            start_service_stop = ServiceStop.objects.get(service_id=service_id, stop_id=start_stop_id)
            stops = Stop.objects.filter(
                service_stop__service_id=service_id,
                service_stop__direction=start_service_stop.direction,
                service_stop__order__gte=start_service_stop.order
            )
        else:
            stops = Stop.objects.filter(service_stop__service_id=service_id)

        stop_serializer = StopSerializer(stops, many=True)
        json_stops = JSONRenderer().render({"stops": stop_serializer.data})

        return HttpResponse(json_stops, content_type='application/json')


class StopsWithinRadius(APIView):
    permission_classes = (permissions.IsAuthenticated, )
    authentication_classes = (JSONWebTokenAuthentication, )

    def get(self, request):
        user_latitude = float(request.GET.get("latitude", ""))
        user_longitude = float(request.GET.get("longitude", ""))
        radius = float(request.GET.get("radius", ""))

        earth_radius = 6378
        latitude_offset = (radius / earth_radius) * (180 / math.pi)
        longitude_offset = (radius / earth_radius) * (180 / math.pi) / math.cos(user_latitude * math.pi/180)

        latitude_top_offset = user_latitude + latitude_offset
        longitude_right_offset = user_longitude + longitude_offset

        latitude_bottom_offset = user_latitude - latitude_offset
        longitude_left_offset = user_longitude - longitude_offset

        closest_stops = Stop.objects.filter(latitude__gte=latitude_bottom_offset,
                                            latitude__lte=latitude_top_offset,
                                            longitude__gte=longitude_left_offset,
                                            longitude__lte=longitude_right_offset).prefetch_related()

        stop_serializer = StopSerializer(closest_stops, many=True)
        json_stops = JSONRenderer().render({"stops": stop_serializer.data})

        return HttpResponse(json_stops, content_type='application/json')


class RideView(APIView):

    permission_classes = (permissions.IsAuthenticated, )
    authentication_classes = (JSONWebTokenAuthentication, )

    def post(self, request):
        journey_id = request.POST.get("journey_id", None)
        new_ride = request.POST.get("ride", None)

        if not new_ride:
            error_message = "No ride was provided as part of request parameters."
            return HttpResponseBadRequest(error_message, content_type='application/json')

        new_ride = json.loads(new_ride)

        # Check if Start Stop exists
        start_stop = Stop.objects.get_or_none(id=new_ride["start_stop_id"])
        if not start_stop:
            error_message = "No existing stop could be found with ID {}".format(new_ride["start_stop_id"])
            return HttpResponseBadRequest(error_message, content_type='application/json')

        # Check if End Stop exists
        end_stop = Stop.objects.get_or_none(id=new_ride["end_stop_id"])
        if not end_stop:
            error_message = "No existing stop could be found with ID {}".format(new_ride["end_stop_id"])
            return HttpResponseBadRequest(error_message, content_type='application/json')

        # Check if Service exists
        service = Service.objects.get_or_none(id=new_ride["service_id"])
        if not service:
            error_message = "No existing service could be found with ID {}".format(new_ride["service_id"])
            return HttpResponseBadRequest(error_message, content_type='application/json')

        start_time = timezone.make_aware(dateutil.parser.parse(new_ride["start_time"]), timezone.get_current_timezone())
        end_time = timezone.make_aware(dateutil.parser.parse(new_ride["end_time"]), timezone.get_current_timezone())

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

            # Add journey to the user's diary
            journey.account = request.user
            journey.save()

        # Check if optinal parameters exist
        people_waiting = -1
        if "people_waiting" in new_ride:
            people_waiting = new_ride["people_waiting"]

        people_boarding = -1
        if "people_boarding" in new_ride:
            people_boarding = new_ride["people_boarding"]

        # Create a new ride for a journey
        with transaction.atomic():
            journey.rides.create(
                start_time=start_time,
                end_time=end_time,
                start_stop=start_stop,
                end_stop=end_stop,
                service=service,
                wait_duration=new_ride["wait_duration"],
                travel_duration=new_ride["travel_duration"],
                distance=new_ride["distance"],
                seat=new_ride["seat"],
                greet=new_ride["greet"],
                rating=new_ride["rating"],
                people_waiting=people_waiting,
                people_boarding=people_boarding
            )

        response_data = json.dumps({"journey_id": journey.id})

        return HttpResponse(response_data, content_type='application/json')


class QuestionnaireView(APIView):

    permission_classes = (permissions.IsAuthenticated, )
    authentication_classes = (JSONWebTokenAuthentication, )

    def post(self, request):
        questionnaire_json = request.POST.get("questionnaire", None)

        questionnaire = json.loads(questionnaire_json)

        age = questionnaire["age"]
        gender = questionnaire["gender"]
        concession_card = questionnaire["concession_card"]
        travel_reason = questionnaire["travel_reason"]

        Questionnaire.objects.create(age=age,
                                     gender=gender,
                                     concession_card=concession_card,
                                     travel_reason=travel_reason)

        response_data = json.dumps({'success': True})

        return HttpResponse(response_data, content_type='application/json')


@csrf_exempt
def get_diary_for_user(request):
    username = request.GET.get("username", "")

    try:
        journeys = Journey.objects.filter(account__username=username).prefetch_related('rides')
    except ObjectDoesNotExist:
        error_message = "User with username '{}' could not be found.".format(username)
        return HttpResponseBadRequest(error_message, content_type='application/json')

    journey_serliazer = JourneySerializer(journeys, many=True)
    json_journeys = JSONRenderer().render({"journeys": journey_serliazer.data})

    return HttpResponse(json_journeys, content_type='application/json')


class ServiceView(APIView):

    permission_classes = (permissions.IsAuthenticated, )
    authentication_classes = (JSONWebTokenAuthentication, )

    def get(self, request):
        services = Service.objects.all().order_by('id')
        service_serializer = ServiceSerializer(services, many=True)
        json_services = JSONRenderer().render({"services": service_serializer.data})

        return HttpResponse(json_services, content_type='application/json')


class IndexView(APIView):
    permission_classes = (permissions.AllowAny, )

    def get(self, request):
        template = loader.get_template('busfeedback/index.html')

        return HttpResponse(template.render())
