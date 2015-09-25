from django.http.response import Http404, HttpResponse
import requests
from busfeedback.constants import *
from busfeedback.utilities.bus_updater import update_services_and_stops


def get_data(request):
    update_services_and_stops()

    return HttpResponse("Done", content_type='application/json')
