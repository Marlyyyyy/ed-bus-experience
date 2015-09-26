from django.test import TestCase
from busfeedback.utilities.bus_updater import *
from busfeedback.constants import *
from datetime import datetime, timedelta
from django.utils import timezone
from busfeedback.models.service import Service
from busfeedback.models.stop import Stop
from django.test import Client


class BusUpdaterTestCase(TestCase):

    def test_check_if_need_update(self):
        update_table_date("services_table", timezone.now())

        need_for_update = check_if_need_update("services_table", timezone.now(), SERVICES_UPDATE_RATE)
        self.assertFalse(need_for_update, "Does not need update immediately after inserting.")

        need_for_update = check_if_need_update("services_table", timezone.now()+timedelta(hours=1), SERVICES_UPDATE_RATE)
        self.assertFalse(need_for_update, "Does not need update 1 hour after inserting.")

        need_for_update = check_if_need_update("services_table", timezone.now()+timedelta(days=1), SERVICES_UPDATE_RATE)
        self.assertTrue(need_for_update, "Needs an update 1 day after inserting.")

    def test_update_services_and_stops(self):
        services = Service.objects.all()
        self.assertFalse(services, "Services should be empty.")

        stops = Stop.objects.all()
        self.assertFalse(stops, "Stops should be empty.")

        update_services_and_stops()

        services = Service.objects.all()
        self.assertGreater(services.count(), 0, "Services should not be empty.")

        first_service_stops = services[0].stops
        self.assertGreater(first_service_stops.count(), 0, "Stops should not be empty.")


class BusViewTestCase(TestCase):

    def setUp(self):
        stop = Stop.objects.create(stop_id=95624796, latitude=55.900, longitude=-3.3)
        service = Service.objects.create(name="3", type="day", description="Clovenstone - Mayfield")
        stop.services.add(service)

    def test_get_services_for_stop(self):
        client = Client()
        response = client.post('/bus/api/get_services_for_stop', {'stop_id': '95624796'})
        response_content = json.loads(response.content.decode('utf-8'))
        self.assertEqual("Clovenstone - Mayfield", response_content[0]["description"], "Response should contain the description of the service.")
