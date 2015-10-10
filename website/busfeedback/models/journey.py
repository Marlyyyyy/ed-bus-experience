from django.db import models
from django.contrib.auth.models import User


class Journey(models.Model):

    start_time = models.DateTimeField()
    end_time = models.DateTimeField()

    account = models.ForeignKey(User, blank=True, null=True, related_name="journeys")

    created_at = models.DateTimeField(auto_now_add=True)

    def __unicode__(self):
        return '{0}'.format(self.id)

    class Meta:
        db_table = 'tbl_busfeedback_journey'
        ordering = ('start_time',)
