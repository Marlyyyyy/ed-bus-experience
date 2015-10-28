# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations


class Migration(migrations.Migration):

    dependencies = [
        ('busfeedback', '0001_initial'),
    ]

    operations = [
        migrations.AddField(
            model_name='stop',
            name='name',
            field=models.CharField(default='Default', max_length=100),
        ),
        migrations.AddField(
            model_name='stop',
            name='orientation',
            field=models.FloatField(default=0.0),
        ),
    ]
