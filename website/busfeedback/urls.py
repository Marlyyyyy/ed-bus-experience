from django.conf.urls import patterns, url
from busfeedback.views import get_services_for_stop, get_closest_stops


urlpatterns = patterns(
    '',
    url(r'^api/get_services_for_stop$', get_services_for_stop, name='get_services_for_stop'),
    url(r'^api/get_closest_stops', get_closest_stops, name='get_closest_stops'),
)