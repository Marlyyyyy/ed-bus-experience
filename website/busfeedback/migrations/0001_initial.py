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
                ('id', models.AutoField(verbose_name='ID', auto_created=True, primary_key=True, serialize=False)),
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
                ('id', models.AutoField(verbose_name='ID', auto_created=True, primary_key=True, serialize=False)),
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
            name='Stop',
            fields=[
                ('id', models.AutoField(verbose_name='ID', auto_created=True, primary_key=True, serialize=False)),
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
                ('id', models.AutoField(verbose_name='ID', auto_created=True, primary_key=True, serialize=False)),
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
                ('id', models.AutoField(verbose_name='ID', auto_created=True, primary_key=True, serialize=False)),
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
