package com.marton.edibus.services;

import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.inject.Inject;
import com.marton.edibus.enums.CurrentActivityEnum;
import com.marton.edibus.events.LocationUpdatedEvent;
import com.marton.edibus.models.Stop;
import com.marton.edibus.events.TrackerStateUpdatedEvent;
import com.marton.edibus.utilities.DistanceCalculator;
import com.marton.edibus.utilities.JourneyManager;

import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.event.EventBus;
import roboguice.service.RoboService;


public class LocationProcessorService extends RoboService {

    @Inject
    private JourneyManager journeyManager;
    
    private static final double START_STOP_DISTANCE_THRESHOLD = 50;

    private static final double END_STOP_DISTANCE_THRESHOLD = 50;

    private EventBus eventBus = EventBus.getDefault();

    private TrackerStateUpdatedEvent trackerStateUpdatedEvent;

    private Timer timer;

    private CurrentActivityEnum currentActivityEnum = CurrentActivityEnum.WAITING;

    private int waitingSeconds = 0;

    private int travellingSeconds = 0;

    public LocationProcessorService() {
        this.eventBus.register(this);
        this.trackerStateUpdatedEvent = new TrackerStateUpdatedEvent();
    }

    @Override
    public void onCreate(){
        super.onCreate();

        this.timer = new Timer();
        this.timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {

                switch(currentActivityEnum){
                    case WAITING:
                        waitingSeconds++;
                        break;

                    case TRAVELLING:
                        travellingSeconds++;
                        break;
                }
            }
        }, 0, 1000);
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

        Stop startStop = this.journeyManager.getTrip().getStartStop();
        double distanceFromStart = DistanceCalculator.getDistanceBetweenPoints(
                latitude, longitude, startStop.getLatitude(), startStop.getLongitude());

        switch (this.journeyManager.getJourneyState()){
            case RUNNING:

                this.trackerStateUpdatedEvent.setDistanceFromGoal(remainingDistance);
                this.trackerStateUpdatedEvent.setDistanceFromStart(distanceFromStart);
                this.trackerStateUpdatedEvent.setWaitingTime(this.waitingSeconds*1000);
                this.trackerStateUpdatedEvent.setTravellingTime(this.travellingSeconds*1000);
        }

        this.trackerStateUpdatedEvent.setLatitude(latitude);
        this.trackerStateUpdatedEvent.setLongitude(longitude);

        // Check if the user has left their start-stop
        if (distanceFromStart < START_STOP_DISTANCE_THRESHOLD){
            this.currentActivityEnum = CurrentActivityEnum.WAITING;
        }else{
            this.currentActivityEnum = CurrentActivityEnum.TRAVELLING;
        }

        this.trackerStateUpdatedEvent.setCurrentActivityEnum(this.currentActivityEnum);

        this.eventBus.post(trackerStateUpdatedEvent);

        // If the user has arrived the their destination
        if (remainingDistance < END_STOP_DISTANCE_THRESHOLD){
            this.journeyManager.finishTrip();
        }
    }
}
