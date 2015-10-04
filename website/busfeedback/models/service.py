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
    stops = models.ManyToManyField(Stop, related_name='services')
    name = models.CharField(max_length=5, blank=False, unique=True)
    type = models.CharField(max_length=40, blank=False)
    description = models.CharField(max_length=100, blank=False)

    objects = ServiceManager()

    def __unicode__(self):
        return self.name

    class Meta:
        db_table = 'tbl_busfeedback_service'
        ordering = ('name',)