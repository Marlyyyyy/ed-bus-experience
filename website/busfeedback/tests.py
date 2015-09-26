from django.test import TestCase
from busfeedback.utilities.bus_updater import *
from busfeedback.constants import *
from datetime import datetime, timedelta
from django.utils import timezone
from busfeedback.models.service import Service
from busfeedback.models.stop import Stop


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
        self.assertGreater(services.count(), 0, "Services should be empty.")

        first_service_stops = services[0].stops
        self.assertGreater(first_service_stops.count(), 0, "Stops should be empty.")