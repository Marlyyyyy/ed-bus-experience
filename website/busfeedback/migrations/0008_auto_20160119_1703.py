# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations


class Migration(migrations.Migration):

    dependencies = [
        ('busfeedback', '0007_questionnaire'),
    ]

    operations = [
        migrations.AlterUniqueTogether(
            name='servicestop',
            unique_together=set([('service', 'stop')]),
        ),
    ]
