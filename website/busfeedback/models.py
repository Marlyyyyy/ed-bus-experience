from django.db import models


class Stop(models.Model):

    name = models.CharField(max_length=100, blank=False, unique=True)
    latitude = models.FloatField()
    longitude = models.FloatField()

    def __unicode__(self):
        return self.name

    class Meta:
        db_table = 'tbl_busfeedback_stop'


class Service(models.Model):

    # Many to many relationship with Stop
    stops = models.ManyToManyField(Stop)
    name = models.CharField(max_length=5, blank=False, unique=True)
    type = models.CharField(max_length=40, blank=False)
    description = models.CharField(max_length=100, blank=False)

    def __unicode__(self):
        return self.name

    class Meta:
        db_table = 'tbl_busfeedback_service'
        ordering = ('name',)


class Journey(models.Model):

    start_time = models.DateTimeField()
    end_time = models.DateTimeField()

    created_at = models.DateTimeField(auto_now_add=True)

    def __unicode__(self):
        return '{0}'.format(self.id)

    class Meta:
        db_table = 'tbl_busfeedback_journey'
        ordering = ('start_time',)


class Trip(models.Model):

    start_time = models.DateTimeField()
    end_time = models.DateTimeField()

    # Many to one relationship with Journey
    journey = models.ForeignKey(Journey, related_name='trips', null=True)

    # Many to one relationships with Stop
    start_stop = models.ForeignKey(Stop, related_name='+')
    end_stop = models.ForeignKey(Stop, related_name='+')

    # Many to one relationship with Service
    service = models.ForeignKey(Service)

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