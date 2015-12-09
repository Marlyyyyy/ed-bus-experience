from rest_framework import serializers
from busfeedback.models.service import ServiceStop


class ServiceStopSerializer(serializers.ModelSerializer):

    class Meta:
        model = ServiceStop

        fields = ('direction', 'order')

    def get_validation_exclusions(self, *args, **kwargs):
        exclusions = super(ServiceStopSerializer, self).get_validation_exclusions()

        return exclusions
