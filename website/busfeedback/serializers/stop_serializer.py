from rest_framework import serializers
from busfeedback.models.stop import Stop
from busfeedback.serializers.service_serializer import ServiceSerializer
from busfeedback.serializers.servicestop_serializer import ServiceStopSerializer


class StopSerializer(serializers.ModelSerializer):
    services = ServiceSerializer(many=True)

    class Meta:
        model = Stop

        fields = ('id', 'stop_id', 'name', 'latitude', 'longitude', 'orientation', 'services')
        read_only_fields = ('id')

    def get_validation_exclusions(self, *args, **kwargs):
        exclusions = super(StopSerializer, self).get_validation_exclusions()

        return exclusions + ['services']
