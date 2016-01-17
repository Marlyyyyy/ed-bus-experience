from django.conf.urls import patterns, url
from busfeedback.views.bus_view import RideView, get_diary_for_user,\
    StopsForServiceView, ServiceView, ServicesForStopView, StopsWithinRadius, QuestionnaireView
from busfeedback.views.statistics_view import GeneralStatisticsView


urlpatterns = patterns(
    '',
    url(r'^api/services_for_stop', ServicesForStopView.as_view(), name='services_for_stop'),
    url(r'^api/stops_within_radius', StopsWithinRadius.as_view(), name='stops_within_radius'),
    url(r'^api/stops_for_service', StopsForServiceView.as_view(), name='stops_for_service'),
    url(r'^api/ride', RideView.as_view(), name='ride'),
    url(r'^api/get_diary_for_user', get_diary_for_user, name='get_diary_for_user'),
    url(r'^api/service', ServiceView.as_view(), name='service'),
    url(r'^api/questionnaire', QuestionnaireView.as_view(), name='questionnaire'),
    url(r'^api/general_statistics', GeneralStatisticsView.as_view(), name='general_statistics'),
)
