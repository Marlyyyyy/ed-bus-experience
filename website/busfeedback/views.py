from django.http.response import Http404, HttpResponse, HttpResponseBadRequest
import json
from busfeedback.utilities.bus_updater import update_services_and_stops, delete_services_stops
from busfeedback.models.service import Service, ServiceStop
from busfeedback.models.journey import Journey
from busfeedback.models.stop import Stop
from rest_framework.views import APIView
from rest_framework import viewsets, status, permissions
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


@csrf_exempt
def get_data(request):
    update_services_and_stops()

    return HttpResponse("{'data': 'Done'}", content_type='application/json')


@csrf_exempt
def remove_data(request):
    delete_services_stops()

    return HttpResponse("Done", content_type='application/json')


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


class ClosestStops(APIView):

    permission_classes = (permissions.IsAuthenticated, )
    authentication_classes = (JSONWebTokenAuthentication, )

    def get(self, request):
        user_latitude = float(request.GET.get("latitude", ""))
        user_longitude = float(request.GET.get("longitude", ""))
        number_of_stops = int(request.GET.get("number_of_stops", ""))

        closest_stops = get_closest_stops(latitude=user_latitude, longitude=user_longitude, number_of_stops=number_of_stops)
        stop_serializer = StopSerializer(closest_stops, many=True)
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


class TripView(APIView):

    permission_classes = (permissions.IsAuthenticated, )
    authentication_classes = (JSONWebTokenAuthentication, )

    def post(self, request):
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

            # Add journey to the user's diary
            journey.account = request.user
            journey.save()

        # Check if optinal parameters exist
        people_waiting = -1
        if "people_waiting" in new_trip:
            people_waiting = new_trip["people_waiting"]

        people_boarding = -1
        if "people_boarding" in new_trip:
            people_boarding = new_trip["people_boarding"]

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
                rating=new_trip["rating"],
                people_waiting=people_waiting,
                people_boarding=people_boarding
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


class ServiceView(APIView):

    permission_classes = (permissions.IsAuthenticated, )
    authentication_classes = (JSONWebTokenAuthentication, )

    def get(self, request):
        services = Service.objects.all().order_by('id')
        service_serializer = ServiceSerializer(services, many=True)
        json_services = JSONRenderer().render({"services": service_serializer.data})

        return HttpResponse(json_services, content_type='application/json')


def get_closest_stops(latitude, longitude, radius_km=50.0, number_of_stops=3):
        parameters = {
            'latitude': latitude,
            'longitude': longitude,
            'radius_km': radius_km,
            'number_of_stops': number_of_stops
        }

        closest_stops = Stop.objects.raw('''
                            SELECT id, stop_id, latitude, longitude, distance
                            FROM (
                                SELECT
                                    s.id, s.stop_id, s.latitude, s.longitude,
                                    p.radius,
                                    p.distance_unit
                                             * DEGREES(ACOS(COS(RADIANS(p.latpoint))
                                             * COS(RADIANS(s.latitude))
                                             * COS(RADIANS(p.longpoint - s.longitude))
                                             + SIN(RADIANS(p.latpoint))
                                             * SIN(RADIANS(s.latitude)))) AS distance
                                FROM tbl_busfeedback_stop AS s
                                    JOIN (   /* these are the query parameters */
                                        SELECT  %(latitude)s  AS latpoint,  %(longitude)s AS longpoint,
                                                %(radius_km)s AS radius,      111.045 AS distance_unit
                                    ) AS p ON 1=1
                                WHERE s.latitude
                                BETWEEN p.latpoint  - (p.radius / p.distance_unit)
                                     AND p.latpoint  + (p.radius / p.distance_unit)
                                AND s.longitude
                                BETWEEN p.longpoint - (p.radius / (p.distance_unit * COS(RADIANS(p.latpoint))))
                                     AND p.longpoint + (p.radius / (p.distance_unit * COS(RADIANS(p.latpoint))))
                             ) AS d
                             WHERE distance <= radius
                             ORDER BY distance
                             LIMIT %(number_of_stops)s
                             ''', parameters)

        return closest_stops

