from django.test import Client
from django.test import TestCase
from busfeedback.models.service import Service
from busfeedback.models.stop import Stop
import json


class BusViewTestCase(TestCase):
    client = Client()

    def setUp(self):
        stop = Stop.objects.create(stop_id=95624796, latitude=55.90064, longitude=-3.39297)
        service = Service.objects.create(name="3", type="day", description="Clovenstone - Mayfield")
        stop.services.add(service)

    def test_get_services_for_stop(self):
        response = self.client.post('/bus/api/get_services_for_stop', {'stop_id': '95624796'})
        response_content = json.loads(response.content.decode('utf-8'))
        self.assertEqual("Clovenstone - Mayfield", response_content[0]["description"], "Response should contain the description of the service.")

    def test_get_closest_stops(self):
        # Each stop is farther and farther from the user
        Stop.objects.create(stop_id=95624797, latitude=27.710298, longitude=85.322099)
        Stop.objects.create(stop_id=95624798, latitude=27.713641, longitude=85.315447)
        Stop.objects.create(stop_id=95624799, latitude=27.728324, longitude=85.304139)
        Stop.objects.create(stop_id=95624800, latitude=27.736909, longitude=85.280621)
        user_latitude = 27.710184
        user_longitude = 85.323429

        post_parameters = {
            'latitude': user_latitude,
            'longitude': user_longitude,
            'number_of_stops': 2
        }
        response = self.client.post('/bus/api/get_closest_stops', post_parameters)
        response_content = json.loads(response.content.decode('utf-8'))

        self.assertEqual(len(response_content), 2, "Only two stops should be returned.")
        self.assertEqual(response_content[0]["stop_id"], 95624797, "Response should contain the closest stop.")
        self.assertEqual(response_content[1]["stop_id"], 95624798, "Response should contain the second closest stop.")

