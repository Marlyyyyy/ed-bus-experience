from rest_framework_nested import routers
from authentication.views import AccountViewSet, LoginView, LogoutView
from django.conf.urls import patterns, include, url
from django.contrib import admin
from rest_framework.authtoken import views

router = routers.SimpleRouter()

router.register(r'accounts', AccountViewSet)

accounts_router = routers.NestedSimpleRouter(
    router, r'accounts', lookup='account'
)

urlpatterns = patterns(
    '',
    url(r'^api/', include(router.urls)),
    url(r'^api/', include(accounts_router.urls)),
    url(r'^api/login/$', LoginView.as_view(), name='login'),
    url(r'^api/logout/$', LogoutView.as_view(), name='logout'),
    url(r'^api/get_token/$', 'rest_framework_jwt.views.obtain_jwt_token'),
    url(r'^admin/', include(admin.site.urls)),
)
