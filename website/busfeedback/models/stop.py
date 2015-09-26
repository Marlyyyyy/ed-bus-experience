from django.db import models


class StopManager(models.Manager):
    def delete_everything(self):
        Stop.objects.all().delete()


class Stop(models.Model):

    name = models.IntegerField(blank=False, unique=True)
    latitude = models.FloatField()
    longitude = models.FloatField()

    objects = StopManager()

    def __unicode__(self):
        return self.name

    class Meta:
        db_table = 'tbl_busfeedback_stop'