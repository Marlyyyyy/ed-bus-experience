from django.db import models


class Update(models.Model):

    table_name = models.CharField(max_length=40, blank=False)
    last_updated = models.DateTimeField()

    def __unicode__(self):
        return self.table_name

    class Meta:
        db_table = 'tbl_busfeedback_update'
        ordering = ('table_name',)
