package com.marton.edibus.services;

import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.inject.Inject;
import com.marton.edibus.enums.CurrentActivityEnum;
import com.marton.edibus.enums.JourneyStateEnum;
import com.marton.edibus.events.LocationUpdatedEvent;
import com.marton.edibus.events.TimerUpdatedEvent;
import com.marton.edibus.models.Stop;
import com.marton.edibus.events.TrackerStateUpdatedEvent;
import com.marton.edibus.models.Ride;
import com.marton.edibus.utilities.GpsCalculator;
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

    private TrackerStateUpdatedEvent previousTrackerStateUpdatedEvent;

    private TrackerStateUpdatedEvent trackerStateUpdatedEvent;

    private long latestUpdateTime;

    private TimerUpdatedEvent timerUpdatedEvent;

    private Timer timer;

    private CurrentActivityEnum currentActivityEnum = CurrentActivityEnum.PREPARING;

    private int waitingSeconds = 0;

    private int travellingSeconds = 0;

    public LocationProcessorService() {
        this.eventBus.register(this);
        this.trackerStateUpdatedEvent = new TrackerStateUpdatedEvent();
        this.previousTrackerStateUpdatedEvent = new TrackerStateUpdatedEvent();
        this.timerUpdatedEvent = new TimerUpdatedEvent();
    }

    @Override
    public void onCreate(){
        super.onCreate();

        this.timer = new Timer();
        this.timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {

                if (!journeyManager.getJourneyState().equals(JourneyStateEnum.RUNNING)){
                    return;
                }

                switch(currentActivityEnum){
                    case WAITING:
                        waitingSeconds++;
                        break;

                    case TRAVELLING:
                        travellingSeconds++;
                        break;
                }

                timerUpdatedEvent.setWaitingMilliseconds(waitingSeconds * 1000);
                timerUpdatedEvent.setTravellingMilliseconds(travellingSeconds * 1000);
                eventBus.post(timerUpdatedEvent);
            }
        }, 0, 1000);
    }

    @Override
    public void onDestroy(){

        // Don't forget to stop the timer
        this.timer.cancel();
        this.waitingSeconds = 0;
        this.travellingSeconds = 0;
        this.currentActivityEnum = CurrentActivityEnum.PREPARING;
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // Handles the location update event
    public void onEvent(LocationUpdatedEvent locationUpdatedEvent){

        this.processLocationUpdate(locationUpdatedEvent.getLatitude(), locationUpdatedEvent.getLongitude());
    }

    private void processLocationUpdate(double latitude, double longitude){

        long currentUpdateTime = System.currentTimeMillis();

        this.trackerStateUpdatedEvent.setLatitude(latitude);
        this.trackerStateUpdatedEvent.setLongitude(longitude);

        // Calculate the remaining distance
        Stop endStop = this.journeyManager.getRide().getEndStop();
        double remainingDistance = GpsCalculator.getDistanceBetweenPoints(
                latitude, longitude, endStop.getLatitude(), endStop.getLongitude()
        );

        switch (this.journeyManager.getJourneyState()){
            case RUNNING:
                this.trackerStateUpdatedEvent.setDistanceFromGoal(remainingDistance);
                this.trackerStateUpdatedEvent.setWaitingTime(this.waitingSeconds*1000);
                this.trackerStateUpdatedEvent.setTravellingTime(this.travellingSeconds * 1000);

                // Update the waiting times on the ride
                Ride ride = this.journeyManager.getRide();
                ride.setWaitDuration(this.waitingSeconds * 1000);
                ride.setTravelDuration(this.travellingSeconds * 1000);

                // Only count distances and speed if the user is travelling
                if (this.currentActivityEnum == CurrentActivityEnum.TRAVELLING){

                    // Calculate travelled distance in metres
                    double pastDistanceDelta = 0.0;
                    double pastDistance = 0.0;
                    double previousDistanceFromStart = this.trackerStateUpdatedEvent.getDistanceFromStart();
                    pastDistanceDelta = GpsCalculator.getDistanceBetweenPoints(
                            this.trackerStateUpdatedEvent.getLatitude(),
                            this.trackerStateUpdatedEvent.getLongitude(),
                            this.previousTrackerStateUpdatedEvent.getLatitude(),
                            this.previousTrackerStateUpdatedEvent.getLongitude());
                    pastDistance = previousDistanceFromStart + pastDistanceDelta;
                    this.trackerStateUpdatedEvent.setDistanceFromStart(pastDistance);

                    ride.setDistance(pastDistance);

                    // Calculate the current speed in metre per second
                    if (this.latestUpdateTime != 0.0 && pastDistanceDelta != 0.0){
                        long timeDelta = (currentUpdateTime - this.latestUpdateTime)/1000;
                        double speed = pastDistanceDelta/timeDelta;
                        this.trackerStateUpdatedEvent.setCurrentSpeed(speed);
                    }

                    // Calculate the maximum speed in metre per second
                    double currentSpeed = this.trackerStateUpdatedEvent.getCurrentSpeed();
                    if (currentSpeed > this.trackerStateUpdatedEvent.getMaximumSpeed()){
                        this.trackerStateUpdatedEvent.setMaximumSpeed(currentSpeed);
                    }

                    // Calculate the average speed in metre per second
                    double averageSpeed = this.trackerStateUpdatedEvent.getDistanceFromStart()/(this.trackerStateUpdatedEvent.getTravellingTime()/1000);
                    this.trackerStateUpdatedEvent.setAverageSpeed(averageSpeed);
                }
        }

        // Check if the user has left their start-stop
        Stop startStop = this.journeyManager.getRide().getStartStop();
        double passedDistance = GpsCalculator.getDistanceBetweenPoints(
                latitude, longitude, startStop.getLatitude(), startStop.getLongitude()
        );

        if (passedDistance < START_STOP_DISTANCE_THRESHOLD){
            this.currentActivityEnum = CurrentActivityEnum.WAITING;
        }else{
            // If the user leaves the bus stop after waiting, then the travelling starts
            if (this.currentActivityEnum.equals(CurrentActivityEnum.WAITING)){
                this.currentActivityEnum = CurrentActivityEnum.TRAVELLING;
            }
        }

        this.trackerStateUpdatedEvent.setCurrentActivityEnum(this.currentActivityEnum);
        this.eventBus.post(this.trackerStateUpdatedEvent);

        // Store the current state for later
        if (!this.currentActivityEnum.equals(CurrentActivityEnum.PREPARING)){
            this.previousTrackerStateUpdatedEvent.copyValuesFrom(this.trackerStateUpdatedEvent);
            this.latestUpdateTime = currentUpdateTime;
        }

        // If the user has arrived at their destination
        // TODO: upload journey automatically
        if (remainingDistance < END_STOP_DISTANCE_THRESHOLD){
            this.journeyManager.finishTrip();
        }
    }
}
