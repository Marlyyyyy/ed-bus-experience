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
                ('id', models.AutoField(auto_created=True, verbose_name='ID', primary_key=True, serialize=False)),
                ('start_time', models.DateTimeField()),
                ('end_time', models.DateTimeField()),
                ('created_at', models.DateTimeField(auto_now_add=True)),
                ('account', models.ForeignKey(to=settings.AUTH_USER_MODEL, blank=True, null=True, related_name='journeys')),
            ],
            options={
                'db_table': 'tbl_busfeedback_journey',
                'ordering': ('start_time',),
            },
        ),
        migrations.CreateModel(
            name='Questionnaire',
            fields=[
                ('id', models.AutoField(auto_created=True, verbose_name='ID', primary_key=True, serialize=False)),
                ('age', models.IntegerField()),
                ('gender', models.CharField(max_length=1, choices=[('M', 'Male'), ('F', 'Female')])),
                ('concession_card', models.BooleanField()),
                ('travel_reason', models.CharField(max_length=1, choices=[('W', 'Work'), ('L', 'Leisure'), ('B', 'Both')])),
                ('created_at', models.DateTimeField(auto_now_add=True)),
            ],
            options={
                'db_table': 'tbl_busfeedback_questionnaire',
                'ordering': ('created_at',),
            },
        ),
        migrations.CreateModel(
            name='Ride',
            fields=[
                ('id', models.AutoField(auto_created=True, verbose_name='ID', primary_key=True, serialize=False)),
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
            ],
            options={
                'db_table': 'tbl_busfeedback_ride',
                'ordering': ('start_time',),
            },
        ),
        migrations.CreateModel(
            name='Service',
            fields=[
                ('id', models.AutoField(auto_created=True, verbose_name='ID', primary_key=True, serialize=False)),
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
                ('id', models.AutoField(auto_created=True, verbose_name='ID', primary_key=True, serialize=False)),
                ('direction', models.IntegerField()),
                ('order', models.IntegerField()),
                ('service', models.ForeignKey(to='busfeedback.Service', related_name='service_stop')),
            ],
            options={
                'db_table': 'tbl_busfeedback_service_stop',
                'ordering': ('order',),
            },
        ),
        migrations.CreateModel(
            name='Stop',
            fields=[
                ('id', models.AutoField(auto_created=True, verbose_name='ID', primary_key=True, serialize=False)),
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
            name='Update',
            fields=[
                ('id', models.AutoField(auto_created=True, verbose_name='ID', primary_key=True, serialize=False)),
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
            field=models.ForeignKey(to='busfeedback.Stop', related_name='service_stop'),
        ),
        migrations.AddField(
            model_name='service',
            name='stops',
            field=models.ManyToManyField(to='busfeedback.Stop', through='busfeedback.ServiceStop', related_name='services'),
        ),
        migrations.AddField(
            model_name='ride',
            name='end_stop',
            field=models.ForeignKey(to='busfeedback.Stop', related_name='+'),
        ),
        migrations.AddField(
            model_name='ride',
            name='journey',
            field=models.ForeignKey(to='busfeedback.Journey', related_name='rides'),
        ),
        migrations.AddField(
            model_name='ride',
            name='service',
            field=models.ForeignKey(to='busfeedback.Service'),
        ),
        migrations.AddField(
            model_name='ride',
            name='start_stop',
            field=models.ForeignKey(to='busfeedback.Stop', related_name='+'),
        ),
        migrations.AlterUniqueTogether(
            name='servicestop',
            unique_together=set([('service', 'stop', 'direction')]),
        ),
    ]
