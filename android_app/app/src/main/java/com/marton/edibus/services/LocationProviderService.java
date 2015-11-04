package com.marton.edibus.services;

import android.app.Service;
import android.content.Intent;
import android.location.Location;

import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.marton.edibus.events.LocationUpdatedEvent;

import de.greenrobot.event.EventBus;


public class LocationProviderService extends Service implements LocationListener, GoogleApiClient.ConnectionCallbacks {

    private static final String TAG = Service.class.getName();

    private EventBus eventBus = EventBus.getDefault();

    private LocationUpdatedEvent locationUpdateEvent;

    private GoogleApiClient googleApiClient;

    LocationRequest locationRequest;

    public LocationProviderService() {
        this.locationUpdateEvent = new LocationUpdatedEvent();
    }

    @Override
    public void onCreate(){

        this.googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();

        this.locationRequest = new LocationRequest();
        this.locationRequest.setInterval(8000);
        this.locationRequest.setFastestInterval(3000);
        this.locationRequest.setPriority(com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY);

        this.googleApiClient.connect();
    }

    @Override
    public void onDestroy()
    {
        this.googleApiClient.disconnect();
        Toast.makeText(this, "LocationProviderService Stopped", Toast.LENGTH_LONG).show();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnected(Bundle bundle) {
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        this.locationUpdateEvent.setLatitude(location.getLatitude());
        this.locationUpdateEvent.setLongitude(location.getLongitude());

        Toast.makeText(this, "Location update yaaay", Toast.LENGTH_LONG).show();

        this.eventBus.post(this.locationUpdateEvent);
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }
}
