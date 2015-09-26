from rest_framework import serializers
from busfeedback.models.service import Service


class ServiceSerializer(serializers.ModelSerializer):

    class Meta:
        model = Service

        fields = ('id', 'name', 'type', 'description')
        read_only_fields = ('id')

    def get_validation_exclusions(self, *args, **kwargs):
        exclusions = super(ServiceSerializer, self).get_validation_exclusions()

        return exclusions + ['stops']
