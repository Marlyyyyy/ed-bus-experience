from rest_framework import serializers
from busfeedback.models.stop import Stop


class StopSerializer(serializers.ModelSerializer):

    class Meta:
        model = Stop

        fields = ('id', 'stop_id', 'latitude', 'longitude')
        read_only_fields = ('id')

    def get_validation_exclusions(self, *args, **kwargs):
        exclusions = super(StopSerializer, self).get_validation_exclusions()

        return exclusions + ['services']
