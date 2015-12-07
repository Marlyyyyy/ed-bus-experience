package com.marton.edibus.services;

import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.inject.Inject;
import com.marton.edibus.enums.CurrentActivityEnum;
import com.marton.edibus.events.LocationUpdatedEvent;
import com.marton.edibus.events.TimerUpdatedEvent;
import com.marton.edibus.models.Stop;
import com.marton.edibus.events.TrackerStateUpdatedEvent;
import com.marton.edibus.models.Trip;
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

    private CurrentActivityEnum currentActivityEnum = CurrentActivityEnum.WAITING;

    private int waitingSeconds = 0;

    private int travellingSeconds = 0;

    public LocationProcessorService() {
        this.eventBus.register(this);
        this.trackerStateUpdatedEvent = new TrackerStateUpdatedEvent();
        this.timerUpdatedEvent = new TimerUpdatedEvent();
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

                timerUpdatedEvent.setWaitingSeconds(waitingSeconds);
                timerUpdatedEvent.setTravellingSeconds(travellingSeconds);
                eventBus.post(timerUpdatedEvent);
            }
        }, 0, 1000);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onEvent(LocationUpdatedEvent locationUpdatedEvent){

        this.processLocationUpdate(locationUpdatedEvent.getLatitude(), locationUpdatedEvent.getLongitude());
    }

    private void processLocationUpdate(double latitude, double longitude){

        long currentUpdateTime = System.currentTimeMillis();

        this.trackerStateUpdatedEvent.setLatitude(latitude);
        this.trackerStateUpdatedEvent.setLongitude(longitude);

        // Calculate the remaining distance
        Stop endStop = this.journeyManager.getTrip().getEndStop();
        double remainingDistance = GpsCalculator.getDistanceBetweenPoints(
                latitude, longitude, endStop.getLatitude(), endStop.getLongitude()
        );

        switch (this.journeyManager.getJourneyState()){
            case RUNNING:
                this.trackerStateUpdatedEvent.setDistanceFromGoal(remainingDistance);
                this.trackerStateUpdatedEvent.setWaitingTime(this.waitingSeconds*1000);
                this.trackerStateUpdatedEvent.setTravellingTime(this.travellingSeconds*1000);

                // Update the waiting times on the trip
                Trip trip = this.journeyManager.getTrip();
                trip.setWaitDuration(this.waitingSeconds * 1000);
                trip.setTravelDuration(this.travellingSeconds * 1000);
                this.journeyManager.setTrip(trip);

                // Calculate travelled distance in metres
                double pastDistanceDelta = 0.0;
                if (this.previousTrackerStateUpdatedEvent != null){
                    double previousDistanceFromStart = this.trackerStateUpdatedEvent.getDistanceFromStart();
                    pastDistanceDelta = GpsCalculator.getDistanceBetweenPoints(
                            this.trackerStateUpdatedEvent.getLatitude(),
                            this.trackerStateUpdatedEvent.getLongitude(),
                            this.previousTrackerStateUpdatedEvent.getLatitude(),
                            this.previousTrackerStateUpdatedEvent.getLongitude());
                    this.trackerStateUpdatedEvent.setDistanceFromStart(previousDistanceFromStart + pastDistanceDelta);
                }else{
                    this.previousTrackerStateUpdatedEvent = new TrackerStateUpdatedEvent();
                }

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
                if (this.currentActivityEnum == CurrentActivityEnum.TRAVELLING){
                    double averageSpeed = this.trackerStateUpdatedEvent.getDistanceFromStart()/(this.trackerStateUpdatedEvent.getTravellingTime()/1000);
                    this.trackerStateUpdatedEvent.setAverageSpeed(averageSpeed);
                }
        }

        // Check if the user has left their start-stop
        Stop startStop = this.journeyManager.getTrip().getStartStop();
        double passedDistance = GpsCalculator.getDistanceBetweenPoints(
                latitude, longitude, startStop.getLatitude(), startStop.getLongitude()
        );

        if (passedDistance < START_STOP_DISTANCE_THRESHOLD){
            this.currentActivityEnum = CurrentActivityEnum.WAITING;
        }else{
            this.currentActivityEnum = CurrentActivityEnum.TRAVELLING;
        }

        this.trackerStateUpdatedEvent.setCurrentActivityEnum(this.currentActivityEnum);

        this.eventBus.post(this.trackerStateUpdatedEvent);

        // Store the current state for later
        this.previousTrackerStateUpdatedEvent.copyValuesFrom(this.trackerStateUpdatedEvent);
        this.latestUpdateTime = currentUpdateTime;

        // If the user has arrived at their destination
        if (remainingDistance < END_STOP_DISTANCE_THRESHOLD){
            this.journeyManager.finishTrip();
        }
    }
}
