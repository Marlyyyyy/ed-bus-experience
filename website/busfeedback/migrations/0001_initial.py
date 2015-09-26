# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations


class Migration(migrations.Migration):

    dependencies = [
    ]

    operations = [
        migrations.CreateModel(
            name='Journey',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
                ('start_time', models.DateTimeField()),
                ('end_time', models.DateTimeField()),
                ('created_at', models.DateTimeField(auto_now_add=True)),
            ],
            options={
                'db_table': 'tbl_busfeedback_journey',
                'ordering': ('start_time',),
            },
        ),
        migrations.CreateModel(
            name='Service',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
                ('name', models.CharField(unique=True, max_length=5)),
                ('type', models.CharField(max_length=40)),
                ('description', models.CharField(max_length=100)),
            ],
            options={
                'db_table': 'tbl_busfeedback_service',
                'ordering': ('name',),
            },
        ),
        migrations.CreateModel(
            name='Stop',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
                ('name', models.IntegerField(unique=True)),
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
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
                ('start_time', models.DateTimeField()),
                ('end_time', models.DateTimeField()),
                ('start_stop_name', models.IntegerField()),
                ('end_stop_name', models.IntegerField()),
                ('service_name', models.CharField(max_length=5)),
                ('wait_duration', models.IntegerField()),
                ('travel_duration', models.IntegerField()),
                ('seat', models.BooleanField()),
                ('rating', models.FloatField()),
                ('created_at', models.DateTimeField(auto_now_add=True)),
                ('updated_at', models.DateTimeField(auto_now=True)),
                ('journey', models.ForeignKey(to='busfeedback.Journey', related_name='trips')),
            ],
            options={
                'db_table': 'tbl_busfeedback_trip',
                'ordering': ('start_time',),
            },
        ),
        migrations.CreateModel(
            name='Update',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
                ('table_name', models.CharField(max_length=40)),
                ('last_updated', models.DateTimeField()),
            ],
            options={
                'db_table': 'tbl_busfeedback_update',
                'ordering': ('table_name',),
            },
        ),
        migrations.AddField(
            model_name='service',
            name='stops',
            field=models.ManyToManyField(to='busfeedback.Stop', related_name='services'),
        ),
    ]