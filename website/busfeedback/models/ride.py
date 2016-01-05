from django.db import models
from busfeedback.models.journey import Journey
from busfeedback.models.stop import Stop
from busfeedback.models.service import Service


class Ride(models.Model):

    start_time = models.DateTimeField()
    end_time = models.DateTimeField()

    # Many to one relationship with Journey
    journey = models.ForeignKey(Journey, related_name='rides')

    # Many to one relationships with Stop
    start_stop = models.ForeignKey(Stop, related_name='+')
    end_stop = models.ForeignKey(Stop, related_name='+')

    # Many to one relationship with Service
    service = models.ForeignKey(Service)

    wait_duration = models.IntegerField()
    travel_duration = models.IntegerField()
    seat = models.BooleanField()
    rating = models.FloatField()
    distance = models.FloatField()
    people_waiting = models.IntegerField(default=-1)
    people_boarding = models.IntegerField(default=-1)
    greet = models.BooleanField()

    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    def __unicode__(self):
        return '{0}'.format(self.id)

    class Meta:
        db_table = 'tbl_busfeedback_ride'
        ordering = ('start_time',)
