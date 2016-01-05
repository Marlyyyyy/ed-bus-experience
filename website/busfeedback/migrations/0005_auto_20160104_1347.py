# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations


class Migration(migrations.Migration):

    dependencies = [
        ('busfeedback', '0004_trip_greet'),
    ]

    operations = [
        migrations.CreateModel(
            name='Ride',
            fields=[
                ('id', models.AutoField(serialize=False, auto_created=True, verbose_name='ID', primary_key=True)),
                ('start_time', models.DateTimeField()),
                ('end_time', models.DateTimeField()),
                ('wait_duration', models.IntegerField()),
                ('travel_duration', models.IntegerField()),
                ('seat', models.BooleanField()),
                ('rating', models.FloatField()),
                ('distance', models.FloatField()),
                ('people_waiting', models.IntegerField(default=-1)),
                ('people_boarding', models.IntegerField(default=-1)),
                ('greet', models.BooleanField()),
                ('created_at', models.DateTimeField(auto_now_add=True)),
                ('updated_at', models.DateTimeField(auto_now=True)),
                ('end_stop', models.ForeignKey(related_name='+', to='busfeedback.Stop')),
                ('journey', models.ForeignKey(related_name='trips', to='busfeedback.Journey')),
                ('service', models.ForeignKey(to='busfeedback.Service')),
                ('start_stop', models.ForeignKey(related_name='+', to='busfeedback.Stop')),
            ],
            options={
                'ordering': ('start_time',),
                'db_table': 'tbl_busfeedback_ride',
            },
        ),
        migrations.RemoveField(
            model_name='trip',
            name='end_stop',
        ),
        migrations.RemoveField(
            model_name='trip',
            name='journey',
        ),
        migrations.RemoveField(
            model_name='trip',
            name='service',
        ),
        migrations.RemoveField(
            model_name='trip',
            name='start_stop',
        ),
        migrations.DeleteModel(
            name='Trip',
        ),
    ]
