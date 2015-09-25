from django.db import models
from busfeedback.models.journey import Journey


class Trip(models.Model):

    start_time = models.DateTimeField()
    end_time = models.DateTimeField()

    # Many to one relationship with Journey
    journey = models.ForeignKey(Journey, related_name='trips')

    # Integrity constraints missing because of the frequent updates of Stop and Service
    start_stop_name = models.IntegerField(blank=False)
    end_stop_name = models.IntegerField(blank=False)
    service_name = models.CharField(max_length=5, blank=False)

    wait_duration = models.IntegerField()
    travel_duration = models.IntegerField()
    seat = models.BooleanField()
    rating = models.FloatField()

    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    def __unicode__(self):
        return '{0}'.format(self.id)

    class Meta:
        db_table = 'tbl_busfeedback_trip'
        ordering = ('start_time',)
