# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations


class Migration(migrations.Migration):

    dependencies = [
        ('busfeedback', '0002_auto_20151211_1423'),
    ]

    operations = [
        migrations.AddField(
            model_name='trip',
            name='distance',
            field=models.FloatField(default=100.5),
            preserve_default=False,
        ),
    ]
