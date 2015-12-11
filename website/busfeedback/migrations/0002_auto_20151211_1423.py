# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations


class Migration(migrations.Migration):

    dependencies = [
        ('busfeedback', '0001_initial'),
    ]

    operations = [
        migrations.AddField(
            model_name='trip',
            name='people_boarding',
            field=models.IntegerField(default=-1),
        ),
        migrations.AddField(
            model_name='trip',
            name='people_waiting',
            field=models.IntegerField(default=-1),
        ),
        migrations.AlterField(
            model_name='servicestop',
            name='service',
            field=models.ForeignKey(to='busfeedback.Service', related_name='service_stop'),
        ),
        migrations.AlterField(
            model_name='servicestop',
            name='stop',
            field=models.ForeignKey(to='busfeedback.Stop', related_name='service_stop'),
        ),
    ]
