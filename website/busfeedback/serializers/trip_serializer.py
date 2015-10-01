from rest_framework import serializers
from busfeedback.models.trip import Trip


class TripSerializer(serializers.ModelSerializer):

    class Meta:
        model = Trip

        fields = ('id', 'start_time', 'end_time', 'wait_duration', 'travel_duration', 'seat', 'rating', 'created_at', 'updated_at')
        read_only_fields = ('id')

    def get_validation_exclusions(self, *args, **kwargs):
        exclusions = super(TripSerializer, self).get_validation_exclusions()

        return exclusions