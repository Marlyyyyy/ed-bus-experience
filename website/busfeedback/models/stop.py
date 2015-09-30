from django.db import models
from bulk_update.manager import BulkUpdateManager


class StopManager(BulkUpdateManager):
    def delete_everything(self):
        Stop.objects.all().delete()

    def get_closest_stops(self, latitude, longitude, radius_km=50.0, number_of_stops=3):
        parameters = {
            'latitude': latitude,
            'longitude': longitude,
            'radius_km': radius_km,
            'number_of_stops': number_of_stops
        }

        closest_stops = Stop.objects.raw('''
                            SELECT id, stop_id, latitude, longitude, distance
                            FROM (
                                SELECT
                                    s.id, s.stop_id, s.latitude, s.longitude,
                                    p.radius,
                                    p.distance_unit
                                             * DEGREES(ACOS(COS(RADIANS(p.latpoint))
                                             * COS(RADIANS(s.latitude))
                                             * COS(RADIANS(p.longpoint - s.longitude))
                                             + SIN(RADIANS(p.latpoint))
                                             * SIN(RADIANS(s.latitude)))) AS distance
                                FROM tbl_busfeedback_stop AS s
                                    JOIN (   /* these are the query parameters */
                                        SELECT  %(latitude)s  AS latpoint,  %(longitude)s AS longpoint,
                                                %(radius_km)s AS radius,      111.045 AS distance_unit
                                    ) AS p ON 1=1
                                WHERE s.latitude
                                BETWEEN p.latpoint  - (p.radius / p.distance_unit)
                                     AND p.latpoint  + (p.radius / p.distance_unit)
                                AND s.longitude
                                BETWEEN p.longpoint - (p.radius / (p.distance_unit * COS(RADIANS(p.latpoint))))
                                     AND p.longpoint + (p.radius / (p.distance_unit * COS(RADIANS(p.latpoint))))
                             ) AS d
                             WHERE distance <= radius
                             ORDER BY distance
                             LIMIT %(number_of_stops)s
                             ''', parameters)

        return closest_stops


class Stop(models.Model):

    stop_id = models.IntegerField(blank=False, unique=True)
    latitude = models.FloatField()
    longitude = models.FloatField()

    objects = StopManager()

    def __unicode__(self):
        return self.name

    class Meta:
        db_table = 'tbl_busfeedback_stop'
