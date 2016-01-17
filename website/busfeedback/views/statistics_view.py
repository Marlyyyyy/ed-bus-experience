from django.http.response import HttpResponseBadRequest
import json
from django.http import HttpResponse, HttpResponseNotFound
from busfeedback.utilities.bus_updater import delete_services_stops
from busfeedback.models.service import Service, ServiceStop
from busfeedback.models.journey import Journey
from busfeedback.models.ride import Ride
from busfeedback.models.stop import Stop
from busfeedback.models.questionnaire import Questionnaire
from django.db.models import Avg, Max, Min, Count
from rest_framework.views import APIView
from rest_framework import permissions
from rest_framework_jwt.authentication import JSONWebTokenAuthentication
from busfeedback.serializers.service_serializer import ServiceSerializer
from busfeedback.serializers.stop_serializer import StopSerializer
from busfeedback.serializers.journey_serializer import JourneySerializer
from rest_framework.renderers import JSONRenderer
import datetime
from django.utils import timezone
from operator import itemgetter
from django.core.exceptions import ObjectDoesNotExist
import dateutil.parser
from django.db import transaction
from django.views.decorators.csrf import csrf_exempt
import math
from django.template import loader


class GeneralStatisticsView(APIView):

    permission_classes = (permissions.IsAuthenticated, )
    authentication_classes = (JSONWebTokenAuthentication, )

    def get(self, request):

        number_of_journeys = Journey.objects.count()
        number_of_trips = Ride.objects.count()
        if number_of_journeys == 0:
            trips_per_journey = 0
        else:
            trips_per_journey = number_of_trips / number_of_journeys

        ride_average_rating = Ride.objects.filter(rating__gt=0.0).aggregate(Avg('rating'))['rating__avg']
        ride_average_travel_duration = Ride.objects.aggregate(Avg('wait_duration'))['wait_duration__avg']
        ride_average_waiting_duration = Ride.objects.aggregate(Avg('travel_duration'))['travel_duration__avg']
        ride_average_distance = Ride.objects.aggregate(Avg('distance'))['distance__avg']
        ride_average_people_waiting = Ride.objects.filter(people_waiting__gt=-1).aggregate(Avg('people_waiting'))['people_waiting__avg']
        ride_average_people_boarding = Ride.objects.filter(people_boarding__gt=-1).aggregate(Avg('people_boarding'))['people_boarding__avg']

        ride_seat_group_by = Ride.objects.values('seat').annotate(seat_count=Count('seat'))
        ride_seat_positives = 0.0
        ride_seat_negatives = 0.0
        for group in ride_seat_group_by:
            if group['seat']:
                ride_seat_positives = group['seat_count']
            else:
                ride_seat_negatives = group['seat_count']

        ride_greet_group_by = Ride.objects.values('greet').annotate(greet_count=Count('greet'))
        ride_greet_positives = 0.0
        ride_greet_negatives = 0.0
        for group in ride_greet_group_by:
            if group['greet']:
                ride_greet_positives = group['greet_count']
            else:
                ride_greet_negatives = group['greet_count']

        statistics_dictionary = {
            'number_of_journeys': number_of_journeys,
            'number_of_trips': number_of_trips,
            'trips_per_journey': trips_per_journey,
            'ride_average_rating': ride_average_rating,
            'ride_average_travel_duration': ride_average_travel_duration,
            'ride_average_waiting_duration': ride_average_waiting_duration,
            'ride_average_distance': ride_average_distance,
            'ride_average_people_waiting': ride_average_people_waiting,
            'ride_average_people_boarding': ride_average_people_boarding,
            'ride_seat_positives': ride_seat_positives,
            'ride_seat_negatives': ride_seat_negatives,
            'ride_greet_positives': ride_greet_positives,
            'ride_greet_negatives': ride_greet_negatives
        }

        return_json = JSONRenderer().render(statistics_dictionary)

        return HttpResponse(return_json, content_type='application/json')


class TimeLineStatisticsView(APIView):

    permission_classes = (permissions.IsAuthenticated, )
    authentication_classes = (JSONWebTokenAuthentication, )

    def get(self, request):

        rides_per_day = Ride.objects.filter(created_at__lte=timezone.now(), created_at__gt=timezone.now()-datetime.timedelta(days=30)).extra(select={'day': 'date( created_at )'}).values('day') \
               .annotate(available=Count('created_at'))

        rides_per_day = list(rides_per_day)

        # Filling in the gaps for missing days
        dates = [x['day'] for x in rides_per_day]
        for d in (timezone.now() - datetime.timedelta(days=x) for x in range(0,30)):
            d = d.strftime("%Y-%m-%d")
            if d not in dates:
                rides_per_day.append({'day': d, 'available': 0})

        sorted_rides_per_day = sorted(rides_per_day, key=itemgetter('day'))

        return_json = JSONRenderer().render(sorted_rides_per_day)

        return HttpResponse(return_json, content_type='application/json')