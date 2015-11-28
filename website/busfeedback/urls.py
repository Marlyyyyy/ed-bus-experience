from django.conf.urls import patterns, url
from busfeedback.views import ClosestStops, TripView, get_diary_for_user,\
    StopsForServiceView, ServiceView, ServicesForStopView, StopsWithinRadius


urlpatterns = patterns(
    '',
    url(r'^api/services_for_stop/$', ServicesForStopView.as_view(), name='services_for_stop'),
    url(r'^api/closest_stops/$', ClosestStops.as_view(), name='closest_stops'),
    url(r'^api/stops_within_radius/$', StopsWithinRadius.as_view(), name='stops_within_radius'),
    url(r'^api/stops_for_service/$', StopsForServiceView.as_view(), name='stops_for_service'),
    url(r'^api/trip/$', TripView.as_view(), name='upload_new_trip'),
    url(r'^api/get_diary_for_user/$', get_diary_for_user, name='get_diary_for_user'),
    url(r'^api/service/$', ServiceView.as_view(), name='all_services'),
)
