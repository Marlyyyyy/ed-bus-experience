from rest_framework import serializers
from busfeedback.models.ride import Ride


class RideSerializer(serializers.ModelSerializer):

    class Meta:
        model = Ride

        fields = ('id', 'start_time', 'end_time', 'wait_duration', 'travel_duration', 'distance', 'seat', 'greet', 'rating', 'created_at', 'updated_at')
        read_only_fields = ('id')

    def get_validation_exclusions(self, *args, **kwargs):
        exclusions = super(RideSerializer, self).get_validation_exclusions()

        return exclusions