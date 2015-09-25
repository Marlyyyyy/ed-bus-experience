from django.contrib import admin
from busfeedback.models import journey, service, stop, trip, update

# Register your models here.
admin.site.register(journey.Journey)
admin.site.register(service.Service)
admin.site.register(stop.Stop)
admin.site.register(trip.Trip)
admin.site.register(update.Update)
