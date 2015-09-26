from django.http.response import Http404, HttpResponse
import requests
from busfeedback.utilities.bus_updater import update_services_and_stops, delete_services_stops
from busfeedback.models.service import Service
from busfeedback.models.stop import Stop
from busfeedback.serializers.service_serializer import ServiceSerializer
from rest_framework.renderers import JSONRenderer


def get_data(request):
    update_services_and_stops()

    return HttpResponse("Done", content_type='application/json')


def remove_data(request):
    delete_services_stops()

    return HttpResponse("Done", content_type='application/json')


def get_services_for_stop(request):
    stop_id = int(request.POST.get("stop_id", ""))
    stop = Stop.objects.filter(stop_id=stop_id)[0]
    services = list(stop.services.all())
    service_serializer = ServiceSerializer(services, many=True)
    json_services = JSONRenderer().render(service_serializer.data)
    return HttpResponse(json_services, content_type='application/json')
