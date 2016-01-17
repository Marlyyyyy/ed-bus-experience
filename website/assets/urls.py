from django.conf import settings
from django.conf.urls import patterns, include, url
from django.contrib import admin
from busfeedback.views import get_data, remove_data, IndexView, custom_404

urlpatterns = patterns(
    '',
    url(r'^test', get_data),
    url(r'^remove$', remove_data),
    url(r'^bus/', include('busfeedback.urls')),
    url(r'^auth/', include('authentication.urls')),
    url(r'^admin/', include(admin.site.urls)),
    # url(r'/', custom_404),
    url('^.*$', IndexView.as_view(), name='index')
)

# Silk Profiler
urlpatterns += patterns('', url(r'^silk/', include('silk.urls', namespace='silk')))


# development static media server
if settings.DEBUG:
    urlpatterns += patterns(
        'django.views.static',
        (r'media/(?P<path>.*)',
        'serve',
        {'document_root': settings.MEDIA_ROOT}), )