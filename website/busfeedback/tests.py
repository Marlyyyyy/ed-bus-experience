from django.test import TestCase
from busfeedback.utilities.bus_updater import *
from busfeedback.constants import *
from datetime import datetime, timedelta
from django.utils import timezone


class BusUpdaterTestCase(TestCase):

    def test_check_if_need_update(self):

        update_table_date("services_table", timezone.now())

        need_for_update = check_if_need_update("services_table", timezone.now(), SERVICES_UPDATE_RATE)
        self.assertFalse(need_for_update, "Does not need update immediately after inserting.")

        need_for_update = check_if_need_update("services_table", timezone.now()+timedelta(hours=1), SERVICES_UPDATE_RATE)
        self.assertFalse(need_for_update, "Does not need update 1 hour after inserting.")

        need_for_update = check_if_need_update("services_table", timezone.now()+timedelta(days=1), SERVICES_UPDATE_RATE)
        self.assertTrue(need_for_update, "Needs an update 1 day after inserting.")