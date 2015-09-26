from django.conf.urls import patterns, url
from busfeedback.views import get_services_for_stop


urlpatterns = patterns(
    '',
    url(r'^api/get_services_for_stop$', get_services_for_stop, name='get_services_for_stop'),
)