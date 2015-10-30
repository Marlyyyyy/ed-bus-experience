package com.marton.edibus.services;


import android.content.Intent;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.inject.Inject;
import com.marton.edibus.models.TrackerState;
import com.marton.edibus.utilities.JourneyManager;

import de.greenrobot.event.EventBus;
import roboguice.service.RoboIntentService;

public class LocationProviderService extends RoboIntentService implements LocationListener, GoogleApiClient.ConnectionCallbacks {

    private EventBus eventBus = EventBus.getDefault();

    @Inject
    private JourneyManager journeyManager;

    private TrackerState currentTrackerState;

    private GoogleApiClient googleApiClient;

    LocationRequest locationRequest;

    /**
     * Creates the LocationProvider IntentService.
     *
     */
    public LocationProviderService() {
        super("LocationProviderService");

        this.currentTrackerState = new TrackerState();
    }

    @Override
    public void onCreate(){

        super.onCreate();

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();

        locationRequest = new LocationRequest();
        locationRequest.setInterval(8000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY);

        googleApiClient.connect();
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        while(true){

            // If the service can be safely shut down
            if (this.journeyManager.getFinished()){
                googleApiClient.disconnect();
                break;
            }

            if (!this.journeyManager.getPaused()){

                // TODO: Make sure we only publish the event if we moved a minimum distance
                eventBus.post(currentTrackerState);
            }

            // Sleep this thread for a short time
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        this.currentTrackerState.setLatitude(location.getLatitude());
        this.currentTrackerState.setLongitude(location.getLongitude());
    }

    @Override
    public void onConnected(Bundle bundle) {
        startLocationUpdates();
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}
