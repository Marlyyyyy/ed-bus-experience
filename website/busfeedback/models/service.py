from django.db import models
from busfeedback.models.stop import Stop
from bulk_update.manager import BulkUpdateManager
from django.core.exceptions import ObjectDoesNotExist


class ServiceManager(BulkUpdateManager):
    def delete_everything(self):
        Service.objects.all().delete()

    def get_or_none(self, **kwargs):
        try:
            return self.get(**kwargs)
        except ObjectDoesNotExist:
            return None


class Service(models.Model):

    # Many to many relationship with Stop
    stops = models.ManyToManyField(Stop, related_name='services', through='ServiceStop')

    name = models.CharField(max_length=5, blank=False, unique=True)
    type = models.CharField(max_length=40, blank=False)
    description = models.CharField(max_length=100, blank=False)

    objects = ServiceManager()

    def __unicode__(self):
        return self.name

    class Meta:
        db_table = 'tbl_busfeedback_service'
        ordering = ('name',)


class ServiceStop(models.Model):
    service = models.ForeignKey(Service, related_name='service_stop',  on_delete=models.CASCADE)
    stop = models.ForeignKey(Stop, related_name='service_stop', on_delete=models.CASCADE)
    # Either 0 or 1
    direction = models.IntegerField()
    order = models.IntegerField()

    class Meta:
        db_table = 'tbl_busfeedback_service_stop'
        ordering = ('order',)
        unique_together = ("service", "stop", "direction")
