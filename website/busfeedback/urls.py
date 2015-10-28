from django.conf.urls import patterns, url
from busfeedback.views import get_services_for_stop, get_closest_stops, upload_new_trip, get_diary_for_user, get_stops_for_service


urlpatterns = patterns(
    '',
    url(r'^api/get_services_for_stop$', get_services_for_stop, name='get_services_for_stop'),
    url(r'^api/get_closest_stops', get_closest_stops, name='get_closest_stops'),
    url(r'^api/get_stops_for_service', get_stops_for_service, name='get_stops_for_service'),
    url(r'^api/upload_new_trip', upload_new_trip, name='upload_new_trip'),
    url(r'^api/get_diary_for_user', get_diary_for_user, name='get_diary_for_user'),
)
