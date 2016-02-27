from rest_framework import serializers
from busfeedback.models.ride import Ride
from busfeedback.serializers.stop_serializer import StopSerializer


class RideSerializer(serializers.ModelSerializer):
    start_stop = StopSerializer()
    end_stop = StopSerializer()

    class Meta:
        model = Ride

        fields = ('id', 'start_time', 'end_time', 'wait_duration', 'travel_duration', 'distance', 'seat', 'greet', 'rating', 'created_at', 'updated_at', 'start_stop', 'end_stop')
        read_only_fields = ('id')

    def get_validation_exclusions(self, *args, **kwargs):
        exclusions = super(RideSerializer, self).get_validation_exclusions()

        return exclusions + ['start_stop', 'end_stop']