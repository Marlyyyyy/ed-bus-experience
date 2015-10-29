package com.marton.edibus.services;


import android.content.Intent;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.inject.Inject;
import com.marton.edibus.events.MessageEvent;
import com.marton.edibus.models.TrackerPoint;
import com.marton.edibus.utilities.JourneyManager;

import de.greenrobot.event.EventBus;
import roboguice.service.RoboIntentService;

public class LocationProviderService extends RoboIntentService implements LocationListener, GoogleApiClient.ConnectionCallbacks {

    private EventBus eventBus = EventBus.getDefault();

    @Inject
    private JourneyManager journeyManager;

    private MessageEvent messageEvent;

    private TrackerPoint latestTrackerPoint;

    private GoogleApiClient googleApiClient;

    LocationRequest locationRequest;

    /**
     * Creates an IntentService.
     *
     */
    public LocationProviderService() {
        super("LocationProviderService");

        this.messageEvent = new MessageEvent();

        this.latestTrackerPoint = new TrackerPoint();
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
        int counter = 0;
        while(true){

            // If the service is not paused, do the update
            if (!this.journeyManager.getPaused()){

                if (shouldTripFinish(counter)){

                    this.journeyManager.finishTrip();

                    // Automatically upload trip and/or fire events
                    if (this.journeyManager.getAutomaticFinish())
                    {
                    }else
                    {
                    }
                }else{
                    // Broadcast location updates
                    messageEvent.message = String.valueOf(this.latestTrackerPoint.getLatitude());
                    this.eventBus.post(this.messageEvent);
                    counter++;
                }
            }

            // Sleep this thread for a short time
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // If the service can be safely shut down
            if (this.journeyManager.getFinished()){
                break;
            }
        }
    }

    /**
     * Returns the flag indicating whether the trip is finished.
     * @return A flag indicating whether the trip should be finished or not.
     * */
    // TODO: let this use the latitude and the longitude of the user's current location
    private boolean shouldTripFinish(int counter){
        return counter > 200;
    }

    @Override
    public void onLocationChanged(Location location) {
        this.latestTrackerPoint.setLatitude(location.getLatitude());
        this.latestTrackerPoint.setLongitude(location.getLongitude());
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
