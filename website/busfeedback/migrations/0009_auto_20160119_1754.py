# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations


class Migration(migrations.Migration):

    dependencies = [
        ('busfeedback', '0008_auto_20160119_1703'),
    ]

    operations = [
        migrations.AlterUniqueTogether(
            name='servicestop',
            unique_together=set([('service', 'stop', 'direction')]),
        ),
    ]
