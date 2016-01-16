from django.test import Client
from django.test import TestCase
from busfeedback.models.service import Service, ServiceStop
from busfeedback.models.stop import Stop
from busfeedback.models.journey import Journey
from busfeedback.models.questionnaire import Questionnaire
from django.contrib.auth.models import User
from datetime import datetime, timedelta
import json
from django.core.serializers.json import DjangoJSONEncoder


class BusViewTestCase(TestCase):
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

    def test_get_services_for_stop(self):
        stop = Stop.objects.get(stop_id=95624797)
        response = self.client.get('/bus/api/services_for_stop/', {'id': stop.id}, HTTP_AUTHORIZATION='JWT {}'.format(self.token))
        response_content = json.loads(response.content.decode('utf-8'))
        self.assertEqual("Clovenstone - Mayfield", response_content["services"][0]["description"], "Response should contain the description of the service.")

    def test_get_stops_for_service(self):
        service = Service.objects.get(name="3")
        response = self.client.get('/bus/api/stops_for_service/', {'service_id': service.id}, HTTP_AUTHORIZATION='JWT {}'.format(self.token))
        response_content = json.loads(response.content.decode('utf-8'))
        self.assertEqual(len(response_content), 1, "There should be 3 stops returnd for this service")

    def test_stops_within_radius(self):
        post_parameters = {
            'latitude': self.user_latitude,
            'longitude': self.user_longitude,
            'radius': 4
        }
        response = self.client.get('/bus/api/stops_within_radius/', post_parameters, HTTP_AUTHORIZATION='JWT {}'.format(self.token))
        response_content = json.loads(response.content.decode('utf-8'))

        self.assertEqual(len(response_content["stops"]), 3, "Only two stops should be returned.")
        self.assertEqual(response_content["stops"][0]["stop_id"], 95624797, "Response should contain the closest stop.")
        self.assertEqual(response_content["stops"][1]["stop_id"], 95624798, "Response should contain the second closest stop.")

    def test_upload_new_ride(self):
        self.client.logout()

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
        response = self.client.post('/bus/api/ride/', {'ride': ride_json}, HTTP_AUTHORIZATION='JWT {}'.format(self.token))
        first_response_content = json.loads(response.content.decode('utf-8'))

        self.assertEqual(response.status_code, 200)
        self.assertEqual(Journey.objects.all().count(), 1, "There should only be one journey in the database.")

        # Upload our first ride of a new journey.
        response = self.client.post('/bus/api/ride/', {'ride': ride_json}, HTTP_AUTHORIZATION='JWT {}'.format(self.token))
        second_response_content = json.loads(response.content.decode('utf-8'))

        self.assertEqual(response.status_code, 200)
        self.assertEqual(Journey.objects.all().count(), 2, "There should now be two journeys in the database.")
        self.assertEqual(
            first_response_content['journey_id']+1,
            second_response_content['journey_id'],
            "There should be one more journey in the database than before"
        )

        # Upload our second ride of one of our journeys.
        end_time = datetime.now()
        ride['end_time'] = end_time
        ride_json = json.dumps(ride, cls=DjangoJSONEncoder)

        response = self.client.post('/bus/api/ride/',
                                    {'ride': ride_json,
                                     'journey_id': second_response_content['journey_id']},
                                    HTTP_AUTHORIZATION='JWT {}'.format(self.token))
        third_response_content = json.loads(response.content.decode('utf-8'))

        self.assertEqual(response.status_code, 200)
        self.assertEqual(Journey.objects.all().count(), 2, "There should still be two journeys in the database.")
        self.assertEqual(
            second_response_content['journey_id'],
            third_response_content['journey_id'],
            "There should be the same number of journeys in the database as before."
        )

        second_journey = Journey.objects.get(id=2)

        self.assertEqual(
            second_journey.start_time.strftime("%a, %d %b %Y %H:%M:%S"),
            start_time.strftime("%a, %d %b %Y %H:%M:%S"),
            "The start_time should be the start_time of the first ride."
        )

        self.assertEqual(
            second_journey.end_time.strftime("%a, %d %b %Y %H:%M:%S"),
            end_time.strftime("%a, %d %b %Y %H:%M:%S"),
            "The end_time should be the end_time of the second ride."
        )

        # Upload our first ride of a new journey while logged in.
        self.client.post('/bus/api/ride/', {'ride': ride_json}, HTTP_AUTHORIZATION='JWT {}'.format(self.token))

        user = User.objects.latest('username')
        user_journeys = user.journeys.all()

        self.assertEqual(user_journeys.count(), 3, "The user should have now three journeys in their diary.")

    def test_upload_bad_ride(self):
        start_stop_id = Stop.objects.filter(stop_id=95624797).values_list('id', flat=True)[0]
        end_stop_id = Stop.objects.filter(stop_id=95624798).values_list('id', flat=True)[0]
        service_id = Service.objects.filter(name="3").values_list('id', flat=True)[0]

        current_time = datetime.now()
        ride = {'start_time': current_time, 'end_time': current_time, 'start_stop_id': 0, 'end_stop_id': end_stop_id,
                'service_id': service_id, 'wait_duration': 60000, 'travel_duration': 800000, 'distance':1000.2, 'seat': True, 'rating': 4.5}

        # Bad Start Stop
        ride_json = json.dumps(ride, cls=DjangoJSONEncoder)

        response = self.client.post('/bus/api/ride/', {'ride': ride_json}, HTTP_AUTHORIZATION='JWT {}'.format(self.token))
        self.assertContains(response, "No existing stop could be found with ID", status_code=400)

        # Bad End Stop
        ride['start_stop_id'] = start_stop_id
        ride['end_stop_id'] = 0
        ride_json = json.dumps(ride, cls=DjangoJSONEncoder)

        response = self.client.post('/bus/api/ride/', {'ride': ride_json}, HTTP_AUTHORIZATION='JWT {}'.format(self.token))
        self.assertContains(response, "No existing stop could be found with ID", status_code=400)

        # Bad Service
        ride['end_stop_id'] = end_stop_id
        ride['service_id'] = 0
        ride_json = json.dumps(ride, cls=DjangoJSONEncoder)

        response = self.client.post('/bus/api/ride/', {'ride': ride_json}, HTTP_AUTHORIZATION='JWT {}'.format(self.token))
        self.assertContains(response, "No existing service could be found with ID", status_code=400)

    def test_get_diary_for_user(self):
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
        rides_json = json.dumps(ride, cls=DjangoJSONEncoder)

        # Upload our first ride of a new journey while logged in.
        self.client.post('/bus/api/ride/', {'ride': rides_json}, HTTP_AUTHORIZATION='JWT {}'.format(self.token))

        response = self.client.get('/bus/api/get_diary_for_user/', {'username': 'Heffalumps'}, HTTP_AUTHORIZATION='JWT {}'.format(self.token))
        response_content = json.loads(response.content.decode('utf-8'))

        self.assertEqual(response_content['journeys'][0]['rides'][0]['rating'], 4.5, "The only uploaded ride should have a rating of 4.5")

    def test_get_all_services(self):

        response = self.client.get('/bus/api/service/', {}, HTTP_AUTHORIZATION='JWT {}'.format(self.token))
        response_content = json.loads(response.content.decode('utf-8'))
        self.assertEqual(len(response_content), 1, "There should be 1 service returned")

    def test_proper_questionnaire_upload(self):

        new_questionnaire = {
            'age': 30,
            'gender': 'Male',
            'concession_card': False,
            'travel_reason': 'Both'
        }

        new_questionnaire_json = json.dumps(new_questionnaire, cls=DjangoJSONEncoder)

        # Upload our questionnaire
        response = self.client.post('/bus/api/questionnaire/', {'questionnaire': new_questionnaire_json}, HTTP_AUTHORIZATION='JWT {}'.format(self.token))

        self.assertEqual(response.status_code, 200)

        all_questionnaires = Questionnaire.objects.all()

        self.assertEqual(len(all_questionnaires), 1, "There should only be a single questionnaire created.")
        self.assertEqual(all_questionnaires[0].travel_reason, "Both", "The travel reason should be Both.")
