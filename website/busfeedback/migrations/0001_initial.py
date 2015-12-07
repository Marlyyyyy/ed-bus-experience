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
                ('id', models.AutoField(verbose_name='ID', auto_created=True, serialize=False, primary_key=True)),
                ('start_time', models.DateTimeField()),
                ('end_time', models.DateTimeField()),
                ('created_at', models.DateTimeField(auto_now_add=True)),
                ('account', models.ForeignKey(null=True, related_name='journeys', blank=True, to=settings.AUTH_USER_MODEL)),
            ],
            options={
                'db_table': 'tbl_busfeedback_journey',
                'ordering': ('start_time',),
            },
        ),
        migrations.CreateModel(
            name='Service',
            fields=[
                ('id', models.AutoField(verbose_name='ID', auto_created=True, serialize=False, primary_key=True)),
                ('name', models.CharField(max_length=5, unique=True)),
                ('type', models.CharField(max_length=40)),
                ('description', models.CharField(max_length=100)),
            ],
            options={
                'db_table': 'tbl_busfeedback_service',
                'ordering': ('name',),
            },
        ),
        migrations.CreateModel(
            name='ServiceStop',
            fields=[
                ('id', models.AutoField(verbose_name='ID', auto_created=True, serialize=False, primary_key=True)),
                ('direction', models.IntegerField()),
                ('order', models.IntegerField()),
                ('service', models.ForeignKey(to='busfeedback.Service')),
            ],
            options={
                'db_table': 'tbl_busfeedback_service_stop',
                'ordering': ('order',),
            },
        ),
        migrations.CreateModel(
            name='Stop',
            fields=[
                ('id', models.AutoField(verbose_name='ID', auto_created=True, serialize=False, primary_key=True)),
                ('stop_id', models.IntegerField(unique=True)),
                ('name', models.CharField(max_length=100, default='Default')),
                ('latitude', models.FloatField()),
                ('longitude', models.FloatField()),
                ('orientation', models.FloatField(default=0.0)),
            ],
            options={
                'db_table': 'tbl_busfeedback_stop',
            },
        ),
        migrations.CreateModel(
            name='Trip',
            fields=[
                ('id', models.AutoField(verbose_name='ID', auto_created=True, serialize=False, primary_key=True)),
                ('start_time', models.DateTimeField()),
                ('end_time', models.DateTimeField()),
                ('wait_duration', models.IntegerField()),
                ('travel_duration', models.IntegerField()),
                ('seat', models.BooleanField()),
                ('rating', models.FloatField()),
                ('created_at', models.DateTimeField(auto_now_add=True)),
                ('updated_at', models.DateTimeField(auto_now=True)),
                ('end_stop', models.ForeignKey(to='busfeedback.Stop', related_name='+')),
                ('journey', models.ForeignKey(to='busfeedback.Journey', related_name='trips')),
                ('service', models.ForeignKey(to='busfeedback.Service')),
                ('start_stop', models.ForeignKey(to='busfeedback.Stop', related_name='+')),
            ],
            options={
                'db_table': 'tbl_busfeedback_trip',
                'ordering': ('start_time',),
            },
        ),
        migrations.CreateModel(
            name='Update',
            fields=[
                ('id', models.AutoField(verbose_name='ID', auto_created=True, serialize=False, primary_key=True)),
                ('table_name', models.CharField(max_length=40)),
                ('last_updated', models.DateTimeField()),
            ],
            options={
                'db_table': 'tbl_busfeedback_update',
                'ordering': ('table_name',),
            },
        ),
        migrations.AddField(
            model_name='servicestop',
            name='stop',
            field=models.ForeignKey(to='busfeedback.Stop'),
        ),
        migrations.AddField(
            model_name='service',
            name='stops',
            field=models.ManyToManyField(through='busfeedback.ServiceStop', to='busfeedback.Stop', related_name='services'),
        ),
    ]
