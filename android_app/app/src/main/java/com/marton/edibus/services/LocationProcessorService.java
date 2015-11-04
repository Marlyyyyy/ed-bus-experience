package com.marton.edibus.services;

import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.inject.Inject;
import com.marton.edibus.events.LocationUpdatedEvent;
import com.marton.edibus.models.Stop;
import com.marton.edibus.events.TrackerStateUpdatedEvent;
import com.marton.edibus.utilities.DistanceCalculator;
import com.marton.edibus.utilities.JourneyManager;

import de.greenrobot.event.EventBus;
import roboguice.service.RoboService;


public class LocationProcessorService extends RoboService {

    @Inject
    private JourneyManager journeyManager;
    
    private static final double START_STOP_DISTANCE_THRESHOLD = 50;

    private static final double END_STOP_DISTANCE_THRESHOLD = 50;

    private EventBus eventBus = EventBus.getDefault();

    private TrackerStateUpdatedEvent trackerState;

    public LocationProcessorService() {
        this.eventBus.register(this);
        this.trackerState = new TrackerStateUpdatedEvent();
    }

    @Override
    public void onCreate(){
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onEvent(LocationUpdatedEvent locationUpdateEvent){

        this.processLocationUpdate(locationUpdateEvent.getLatitude(), locationUpdateEvent.getLongitude());
    }

    private void processLocationUpdate(double latitude, double longitude){

        Stop endStop = this.journeyManager.getTrip().getEndStop();
        double remainingDistance = DistanceCalculator.getDistanceBetweenPoints(
                latitude, longitude, endStop.getLatitude(), endStop.getLongitude());
        trackerState.setDistanceFromGoal(remainingDistance);

        Stop startStop = this.journeyManager.getTrip().getStartStop();
        double distanceFromStart = DistanceCalculator.getDistanceBetweenPoints(
                latitude, longitude, startStop.getLatitude(), startStop.getLongitude());
        trackerState.setDistanceFromStart(distanceFromStart);

        this.eventBus.post(trackerState);

        if (remainingDistance < END_STOP_DISTANCE_THRESHOLD){
            this.journeyManager.finishTrip();
        }
    }
}
