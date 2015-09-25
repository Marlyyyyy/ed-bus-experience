from django.db import models


class Stop(models.Model):

    name = models.IntegerField(blank=False, unique=True)
    latitude = models.FloatField()
    longitude = models.FloatField()

    def __unicode__(self):
        return self.name

    class Meta:
        db_table = 'tbl_busfeedback_stop'