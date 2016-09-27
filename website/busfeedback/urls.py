from django.conf.urls import patterns, url
from busfeedback.views.bus_view import RideView, StopsForServiceView, ServiceView, ServicesForStopView,\
    StopsWithinRadius, QuestionnaireView
from busfeedback.views.statistics_view import RideCountTimeLineStatisticsView, RideStatisticsView, \
    YesAndNoTimeLineStatisticsView, AverageTimeLineStatisticsView


urlpatterns = patterns(
    '',
    url(r'^api/services_for_stop', ServicesForStopView.as_view(), name='services_for_stop'),
    url(r'^api/stops_within_radius', StopsWithinRadius.as_view(), name='stops_within_radius'),
    url(r'^api/stops_for_service', StopsForServiceView.as_view(), name='stops_for_service'),
    url(r'^api/ride', RideView.as_view(), name='ride'),
    url(r'^api/service', ServiceView.as_view(), name='service'),
    url(r'^api/questionnaire', QuestionnaireView.as_view(), name='questionnaire'),
    url(r'^api/timeline_statistics', RideCountTimeLineStatisticsView.as_view(), name='timeline_statistics'),
    url(r'^api/bus_statistics', RideStatisticsView.as_view(), name='bus_statistics'),
    url(r'^api/yes_and_no_statistics', YesAndNoTimeLineStatisticsView.as_view(), name='yes_and_no_statistics'),
    url(r'^api/average_statistics', AverageTimeLineStatisticsView.as_view(), name='average_statistics'),
)
