# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations


class Migration(migrations.Migration):

    dependencies = [
        ('busfeedback', '0003_trip_distance'),
    ]

    operations = [
        migrations.AddField(
            model_name='trip',
            name='greet',
            field=models.BooleanField(default=True),
            preserve_default=False,
        ),
    ]
