from django.http import HttpResponse
from busfeedback.models.ride import Ride
from django.db.models import Avg, Count
from rest_framework.views import APIView
from rest_framework import permissions
from rest_framework_jwt.authentication import JSONWebTokenAuthentication
from rest_framework.renderers import JSONRenderer
import datetime
from django.utils import timezone
from operator import itemgetter
from itertools import groupby
import dateutil.parser


class RideStatisticsView(APIView):

    permission_classes = (permissions.IsAuthenticated, )
    authentication_classes = (JSONWebTokenAuthentication, )

    def get(self, request):

        start_stop_id = int(request.GET.get("start_stop_id", -1))
        end_stop_id = int(request.GET.get("end_stop_id", -1))
        service_id = int(request.GET.get("service_id", -1))

        created_at_lte_string = request.GET.get("created_at_lte", "")
        created_at_gt_string = request.GET.get("created_at_gt", "")

        # If no time interval was provided, return all rides.
        if created_at_lte_string == "" and created_at_gt_string == "":
            latest_rides = Ride.objects.all()
        else:
            if created_at_lte_string == "":
                created_at_lte = timezone.now()
            else:
                created_at_lte = timezone.make_aware(dateutil.parser.parse(created_at_lte_string), timezone.get_current_timezone())

            if created_at_gt_string == "":
                created_at_gt = timezone.now()-datetime.timedelta(days=30)
            else:
                created_at_gt = timezone.make_aware(dateutil.parser.parse(created_at_gt_string), timezone.get_current_timezone())

            latest_rides = Ride.objects.filter(
                created_at__lte=created_at_lte,
                created_at__gt=created_at_gt
            )

        if start_stop_id != -1 and end_stop_id !=1:
            latest_rides.filter(start_stop_id=start_stop_id, end_stop_id=end_stop_id)

        if service_id != -1:
            latest_rides.filter(service_id=service_id)

        # Total number of trips and journeys
        number_of_journeys = latest_rides.prefetch_related('journey').distinct().count()
        number_of_rides = latest_rides.count()
        if number_of_journeys == 0:
            trips_per_journey = 0
        else:
            trips_per_journey = number_of_rides / number_of_journeys

        average_rating = latest_rides.filter(rating__gt=0.0).aggregate(Avg('rating'))['rating__avg']
        average_travel_duration = latest_rides.aggregate(Avg('travel_duration'))['travel_duration__avg']
        average_waiting_duration = latest_rides.aggregate(Avg('wait_duration'))['wait_duration__avg']
        average_distance = latest_rides.aggregate(Avg('distance'))['distance__avg']
        average_people_waiting = latest_rides.filter(people_waiting__gt=-1).aggregate(Avg('people_waiting'))['people_waiting__avg']
        average_people_boarding = latest_rides.filter(people_boarding__gt=-1).aggregate(Avg('people_boarding'))['people_boarding__avg']

        seat_group_by = latest_rides.values('seat').annotate(seat_count=Count('seat'))
        seat_positives = 0.0
        seat_negatives = 0.0
        for group in seat_group_by:
            if group['seat']:
                seat_positives = group['seat_count']
            else:
                seat_negatives = group['seat_count']

        greet_group_by = latest_rides.values('greet').annotate(greet_count=Count('greet'))
        greet_positives = 0.0
        greet_negatives = 0.0
        for group in greet_group_by:
            if group['greet']:
                greet_positives = group['greet_count']
            else:
                greet_negatives = group['greet_count']

        statistics_dictionary = {
            'number_of_journeys': number_of_journeys,
            'number_of_rides': number_of_rides,
            'trips_per_journey': trips_per_journey,
            'average_rating': average_rating,
            'average_travel_duration': average_travel_duration,
            'average_waiting_duration': average_waiting_duration,
            'average_distance': average_distance,
            'average_people_waiting': average_people_waiting,
            'average_people_boarding': average_people_boarding,
            'seat_positives': seat_positives,
            'seat_negatives': seat_negatives,
            'greet_positives': greet_positives,
            'greet_negatives': greet_negatives
        }

        return_json = JSONRenderer().render(statistics_dictionary)

        return HttpResponse(return_json, content_type='application/json')


class RideCountTimeLineStatisticsView(APIView):

    permission_classes = (permissions.IsAuthenticated, )
    authentication_classes = (JSONWebTokenAuthentication, )

    def get(self, request):

        rides_per_day = Ride.objects.filter(
            created_at__lte=timezone.now(),
            created_at__gt=timezone.now()-datetime.timedelta(days=30)
        ).extra(select={'day': 'date( created_at )'}).values('day').annotate(available=Count('created_at'))

        rides_per_day = list(rides_per_day)

        # Filling in the gaps for missing days
        dates = [ride['day'] for ride in rides_per_day]
        for day in (timezone.now() - datetime.timedelta(days=x) for x in range(0,30)):
            day = day.strftime("%Y-%m-%d")
            if day not in dates:
                rides_per_day.append({'day': day, 'available': 0})

        sorted_rides_per_date = sorted(rides_per_day, key=itemgetter('day'))
        sorted_rides_per_day = []

        # Group by the dates
        for key, values in groupby(sorted_rides_per_date, key=lambda row: row['day']):
            sum = 0
            for value in values:
                sum += value['available']

            sorted_rides_per_day.append({'day': key, 'available': sum})

        return_json = JSONRenderer().render(sorted_rides_per_day)

        return HttpResponse(return_json, content_type='application/json')


class YesAndNoTimeLineStatisticsView(APIView):

    permission_classes = (permissions.IsAuthenticated, )
    authentication_classes = (JSONWebTokenAuthentication, )

    def get(self, request):

        group_by_value = request.GET.get("group_by_value", "")

        if group_by_value == "":
            return HttpResponse({'error': "Nothing to group by! Please provide a parameter 'group_by_value' in your request."},
                                content_type='application/json')

        rides_group_by_value_and_day = Ride.objects.filter(
            created_at__lte=timezone.now(),
            created_at__gt=timezone.now()-datetime.timedelta(days=30)
        ).extra(select={'day': 'date( created_at )'}).values('day', group_by_value).order_by().annotate(available=Count('created_at'))

        rides_group_by_value_and_day = list(rides_group_by_value_and_day)

        # Create a list of past 30 days
        last_30_day_strings = []
        for x in range(0,30):
            full_date = timezone.now() - datetime.timedelta(days=x)
            last_30_day_strings.append(full_date.strftime("%Y-%m-%d"))

        existing_days = [ride['day'] for ride in rides_group_by_value_and_day]

        # Filling in the gaps for missing days
        for day in last_30_day_strings:
            if day not in existing_days:
                rides_group_by_value_and_day.append({'day': day, 'available': 0, group_by_value: True})
                rides_group_by_value_and_day.append({'day': day, 'available': 0, group_by_value: False})

        sorted_rides_per_date = sorted(rides_group_by_value_and_day, key=itemgetter('day'))

        sorted_rides_per_day = []

        # Group by the dates and the seats
        for key, values in groupby(sorted_rides_per_date, key=lambda row: row['day']):
            sum_of_true = 0
            sum_of_false = 0
            for value in values:
                if value[group_by_value]:
                    sum_of_true += value['available']
                else:
                    sum_of_false += value['available']

            sorted_rides_per_day.append({'day': key, group_by_value: True, 'available': sum_of_true})
            sorted_rides_per_day.append({'day': key, group_by_value: False, 'available': sum_of_false})

        return_json = JSONRenderer().render(sorted_rides_per_day)

        return HttpResponse(return_json, content_type='application/json')


class AverageTimeLineStatisticsView(APIView):

    permission_classes = (permissions.IsAuthenticated, )
    authentication_classes = (JSONWebTokenAuthentication, )

    def get(self, request):

        aggregate_value = request.GET.get("aggregate_value", "")

        if aggregate_value == "":
            return HttpResponse({'error': "Nothing to aggregate on! Please provide a parameter 'aggregate_value' in your request."},
                                content_type='application/json')

        annotated_rides = Ride.objects.filter(
            created_at__lte=timezone.now(),
            created_at__gt=timezone.now()-datetime.timedelta(days=30)
        ).extra(select={'day': 'date( created_at )'}).values('day', aggregate_value).annotate(available=Count('created_at'))

        annotated_rides = list(annotated_rides)

        # Create a list of past 30 days
        last_30_day_strings = []
        for x in range(0,30):
            full_date = timezone.now() - datetime.timedelta(days=x)
            last_30_day_strings.append(full_date.strftime("%Y-%m-%d"))

        existing_days = [ride['day'] for ride in annotated_rides]

        # Filling in the gaps for missing days
        for day in last_30_day_strings:
            if day not in existing_days:
                annotated_rides.append({'day': day, 'available': 0, 'wait_duration':0})

        sorted_rides_per_date = sorted(annotated_rides, key=itemgetter('day'))

        sorted_rides_per_day = []

        # Group by the dates and the seats
        for key, values in groupby(sorted_rides_per_date, key=lambda row: row['day']):
            counter = 0
            sum_of_value = 0
            for value in values:
                sum_of_value += value[aggregate_value]
                counter += 1

            if counter == 0:
                average = 0
            else:
                average = sum_of_value/counter

            sorted_rides_per_day.append({'day': key, 'average': average})

        return_json = JSONRenderer().render(sorted_rides_per_day)

        return HttpResponse(return_json, content_type='application/json')
