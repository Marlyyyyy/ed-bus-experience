# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations


class Migration(migrations.Migration):

    dependencies = [
        ('busfeedback', '0006_auto_20160116_1228'),
    ]

    operations = [
        migrations.CreateModel(
            name='Questionnaire',
            fields=[
                ('id', models.AutoField(verbose_name='ID', auto_created=True, primary_key=True, serialize=False)),
                ('age', models.IntegerField()),
                ('gender', models.CharField(max_length=1, choices=[('M', 'Male'), ('F', 'Female')])),
                ('concession_card', models.BooleanField()),
                ('travel_reason', models.CharField(max_length=1, choices=[('W', 'Work'), ('L', 'Leisure'), ('B', 'Both')])),
                ('created_at', models.DateTimeField(auto_now_add=True)),
            ],
            options={
                'ordering': ('created_at',),
                'db_table': 'tbl_busfeedback_questionnaire',
            },
        ),
    ]
