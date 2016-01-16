# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations


class Migration(migrations.Migration):

    dependencies = [
        ('busfeedback', '0005_auto_20160104_1347'),
    ]

    operations = [
        migrations.AlterField(
            model_name='ride',
            name='journey',
            field=models.ForeignKey(related_name='rides', to='busfeedback.Journey'),
        ),
    ]
