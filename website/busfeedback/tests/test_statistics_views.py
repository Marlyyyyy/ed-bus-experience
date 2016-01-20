from django.test import Client
from django.test import TestCase
from busfeedback.models.service import Service, ServiceStop
from busfeedback.models.stop import Stop
from busfeedback.models.ride import Ride
from busfeedback.models.journey import Journey
from busfeedback.models.questionnaire import Questionnaire
from django.contrib.auth.models import User
from datetime import datetime, timedelta
import json
from django.core.serializers.json import DjangoJSONEncoder


class StatisticsViewTestCase(TestCase):
    client = Client()
    token = None

    def setUp(self):
        self.user_latitude = 27.710184
        self.user_longitude = 85.323429

        # Create new user
        self.response = self.client.post("/auth/api/accounts/", {"username": "Heffalumps", "password": "Woozles"})

        # Set the authentication token
        response = self.client.post("/auth/api/get_token/", {"username": "Heffalumps", "password": "Woozles"})
        response_content = json.loads(response.content.decode('utf-8'))
        self.token = response_content["token"]

        # Each stop is farther and farther from the user
        closest_stop = Stop.objects.create(stop_id=95624797, latitude=27.710298, longitude=85.322099)
        new_stops = [
            Stop(stop_id=95624798, latitude=27.713641, longitude=85.315447),
            Stop(stop_id=95624799, latitude=27.728324, longitude=85.304139),
            Stop(stop_id=95624800, latitude=27.736909, longitude=85.280621)
        ]

        Stop.objects.bulk_create(new_stops)

        service = Service.objects.create(name="3", type="day", description="Clovenstone - Mayfield")
        ServiceStop.objects.create(service=service, stop=closest_stop, direction=0, order=0)

        # Upload a few rides
        start_stop_id = Stop.objects.filter(stop_id=95624797).values_list('id', flat=True)[0]
        end_stop_id = Stop.objects.filter(stop_id=95624798).values_list('id', flat=True)[0]
        service_id = Service.objects.filter(name="3").values_list('id', flat=True)[0]

        current_time = datetime.now()
        start_time = current_time - timedelta(minutes=10)
        end_time = current_time
        ride = {
            'start_time': start_time,
            'end_time': end_time,
            'start_stop_id': start_stop_id,
            'end_stop_id': end_stop_id,
            'service_id': service_id,
            'wait_duration': 60000,
            'travel_duration': 800000,
            'distance':1000.2,
            'greet':True,
            'seat': True,
            'rating': 4.5
        }
        ride_json = json.dumps(ride, cls=DjangoJSONEncoder)

        # Upload our first ride of a new journey.
        self.client.post('/bus/api/ride/', {'ride': ride_json}, HTTP_AUTHORIZATION='JWT {}'.format(self.token))

        ride = {
            'start_time': start_time,
            'end_time': end_time,
            'start_stop_id': start_stop_id,
            'end_stop_id': end_stop_id,
            'service_id': service_id,
            'wait_duration': 10000,
            'travel_duration': 100000,
            'people_boarding': 5,
            'people_waiting': 2,
            'distance': 264,
            'greet': False,
            'seat': True,
            'rating': 0.0
        }
        ride_json = json.dumps(ride, cls=DjangoJSONEncoder)

        # Upload our first ride of a new journey.
        self.client.post('/bus/api/ride/', {'ride': ride_json}, HTTP_AUTHORIZATION='JWT {}'.format(self.token))

    def test_obtain_general_statics(self):

        response = self.client.get('/bus/api/general_statistics/', {}, HTTP_AUTHORIZATION='JWT {}'.format(self.token))
        response_content = json.loads(response.content.decode('utf-8'))

        self.assertEqual(response_content['ride_average_people_boarding'], 5.0, "Boarding people -1 should be ignored")
        self.assertEqual(response_content['number_of_rides'], 2, "Number of rides should be 2.")
        self.assertEqual(response_content['number_of_journeys'], 2, "Number of journeys should be 2.")
        self.assertEqual(response_content['ride_seat_positives'], 2, "Both rides should have a seat.")
        self.assertEqual(response_content['ride_greet_negatives'], 1, "Only one ride didn't greet.")

    def test_obtain_timeline_statics(self):

        response = self.client.get('/bus/api/timeline_statistics/', {}, HTTP_AUTHORIZATION='JWT {}'.format(self.token))
        response_content = json.loads(response.content.decode('utf-8'))

        self.assertEqual(len(response_content), 30, "There should be 30 days in the results set.")

    def test_obtain_averages_between_two_stops(self):

        all_rides = list(Ride.objects.all())
        random_ride = all_rides[0]

        parameters = {
            'start_stop_id': random_ride.start_stop_id,
            'end_stop_id': random_ride.end_stop_id
        }

        response = self.client.get('/bus/api/bus_statistics/', parameters, HTTP_AUTHORIZATION='JWT {}'.format(self.token))
        response_content = json.loads(response.content.decode('utf-8'))

        self.assertEqual(response_content['average_people_waiting'], 2, "-1 People waiting should be ignored.")
        self.assertEqual(response_content['average_people_boarding'], 5, "-1 People boarding should be ignored.")
        self.assertEqual(response_content['average_waiting_duration'], 35000, "The average of two durations: 10k and 60k.")
        self.assertEqual(response_content['average_rating'], 4.5, "The 0.0 rating should be ignored.")
