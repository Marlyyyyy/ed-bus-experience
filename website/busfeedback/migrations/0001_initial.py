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
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
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
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
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
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('name', models.CharField(max_length=100, unique=True)),
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
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('start_time', models.DateTimeField()),
                ('end_time', models.DateTimeField()),
                ('wait_duration', models.IntegerField()),
                ('travel_duration', models.IntegerField()),
                ('seat', models.BooleanField()),
                ('rating', models.FloatField()),
                ('created_at', models.DateTimeField(auto_now_add=True)),
                ('updated_at', models.DateTimeField(auto_now=True)),
                ('end_stop', models.ForeignKey(related_name='+', to='busfeedback.Stop')),
                ('journey', models.ForeignKey(related_name='trips', null=True, to='busfeedback.Journey')),
                ('service', models.ForeignKey(to='busfeedback.Service')),
                ('start_stop', models.ForeignKey(related_name='+', to='busfeedback.Stop')),
            ],
            options={
                'db_table': 'tbl_busfeedback_trip',
                'ordering': ('start_time',),
            },
        ),
        migrations.AddField(
            model_name='service',
            name='stops',
            field=models.ManyToManyField(to='busfeedback.Stop'),
        ),
    ]
