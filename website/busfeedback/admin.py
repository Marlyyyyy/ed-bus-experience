from django.contrib import admin
from busfeedback.models import journey, service, stop, ride, update, questionnaire

# Register your models here.
admin.site.register(journey.Journey)
admin.site.register(service.Service)
admin.site.register(stop.Stop)
admin.site.register(ride.Ride)
admin.site.register(update.Update)
admin.site.register(questionnaire.Questionnaire)
