from django.db import models


class Questionnaire(models.Model):

    age = models.IntegerField()

    GENDER_CHOICES = (
        ('M', 'Male'),
        ('F', 'Female')
    )

    gender = models.CharField(max_length=1, choices=GENDER_CHOICES)

    concession_card = models.BooleanField()

    TRAVEL_REASON_CHOICES = (
        ('W', 'Work'),
        ('L', 'Leisure'),
        ('B', 'Both')
    )

    travel_reason = models.CharField(max_length=1, choices=TRAVEL_REASON_CHOICES)

    created_at = models.DateTimeField(auto_now_add=True)

    def __unicode__(self):
        return '{0}'.format(self.id)

    class Meta:
        db_table = 'tbl_busfeedback_questionnaire'
        ordering = ('created_at',)
