from rest_framework import serializers
from busfeedback.models.journey import Journey
from busfeedback.serializers.trip_serializer import TripSerializer


class JourneySerializer(serializers.ModelSerializer):
    trips = TripSerializer(many=True)

    class Meta:
        model = Journey

        fields = ('id', 'start_time', 'end_time', 'created_at', 'trips')
        read_only_fields = ('id')

    def get_validation_exclusions(self, *args, **kwargs):
        exclusions = super(JourneySerializer, self).get_validation_exclusions()

        return exclusions + ['trips']