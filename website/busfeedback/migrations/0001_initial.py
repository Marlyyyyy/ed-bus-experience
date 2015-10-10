# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations
from django.conf import settings


class Migration(migrations.Migration):

    dependencies = [
        migrations.swappable_dependency(settings.AUTH_USER_MODEL),
    ]

    operations = [
        migrations.CreateModel(
            name='Journey',
            fields=[
                ('id', models.AutoField(serialize=False, auto_created=True, primary_key=True, verbose_name='ID')),
                ('start_time', models.DateTimeField()),
                ('end_time', models.DateTimeField()),
                ('created_at', models.DateTimeField(auto_now_add=True)),
                ('account', models.ForeignKey(related_name='journeys', null=True, to=settings.AUTH_USER_MODEL, blank=True)),
            ],
            options={
                'ordering': ('start_time',),
                'db_table': 'tbl_busfeedback_journey',
            },
        ),
        migrations.CreateModel(
            name='Service',
            fields=[
                ('id', models.AutoField(serialize=False, auto_created=True, primary_key=True, verbose_name='ID')),
                ('name', models.CharField(max_length=5, unique=True)),
                ('type', models.CharField(max_length=40)),
                ('description', models.CharField(max_length=100)),
            ],
            options={
                'ordering': ('name',),
                'db_table': 'tbl_busfeedback_service',
            },
        ),
        migrations.CreateModel(
            name='Stop',
            fields=[
                ('id', models.AutoField(serialize=False, auto_created=True, primary_key=True, verbose_name='ID')),
                ('stop_id', models.IntegerField(unique=True)),
                ('latitude', models.FloatField()),
                ('longitude', models.FloatField()),
            ],
            options={
                'db_table': 'tbl_busfeedback_stop',
            },
        ),
        migrations.CreateModel(
            name='Trip',
            fields=[
                ('id', models.AutoField(serialize=False, auto_created=True, primary_key=True, verbose_name='ID')),
                ('start_time', models.DateTimeField()),
                ('end_time', models.DateTimeField()),
                ('wait_duration', models.IntegerField()),
                ('travel_duration', models.IntegerField()),
                ('seat', models.BooleanField()),
                ('rating', models.FloatField()),
                ('created_at', models.DateTimeField(auto_now_add=True)),
                ('updated_at', models.DateTimeField(auto_now=True)),
                ('end_stop', models.ForeignKey(related_name='+', to='busfeedback.Stop')),
                ('journey', models.ForeignKey(related_name='trips', to='busfeedback.Journey')),
                ('service', models.ForeignKey(to='busfeedback.Service')),
                ('start_stop', models.ForeignKey(related_name='+', to='busfeedback.Stop')),
            ],
            options={
                'ordering': ('start_time',),
                'db_table': 'tbl_busfeedback_trip',
            },
        ),
        migrations.CreateModel(
            name='Update',
            fields=[
                ('id', models.AutoField(serialize=False, auto_created=True, primary_key=True, verbose_name='ID')),
                ('table_name', models.CharField(max_length=40)),
                ('last_updated', models.DateTimeField()),
            ],
            options={
                'ordering': ('table_name',),
                'db_table': 'tbl_busfeedback_update',
            },
        ),
        migrations.AddField(
            model_name='service',
            name='stops',
            field=models.ManyToManyField(related_name='services', to='busfeedback.Stop'),
        ),
    ]
