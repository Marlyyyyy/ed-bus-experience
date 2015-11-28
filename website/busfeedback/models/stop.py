from django.db import models
from bulk_update.manager import BulkUpdateManager
from django.core.exceptions import ObjectDoesNotExist


class StopManager(BulkUpdateManager):
    def delete_everything(self):
        Stop.objects.all().delete()

    def get_or_none(self, **kwargs):
        try:
            return self.get(**kwargs)
        except ObjectDoesNotExist:
            return None


class Stop(models.Model):

    stop_id = models.IntegerField(blank=False, unique=True)
    name = models.CharField(max_length=100, default="Default")
    latitude = models.FloatField()
    longitude = models.FloatField()
    orientation = models.FloatField(default=0.0)

    objects = StopManager()

    def __unicode__(self):
        return self.name

    class Meta:
        db_table = 'tbl_busfeedback_stop'
