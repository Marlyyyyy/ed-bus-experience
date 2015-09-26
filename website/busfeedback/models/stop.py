from django.db import models
from bulk_update.manager import BulkUpdateManager


class StopManager(BulkUpdateManager):
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