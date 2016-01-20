package com.marton.edibus.utilities;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.marton.edibus.App;
import com.marton.edibus.WebCallBack;
import com.marton.edibus.enums.CurrentActivityEnum;
import com.marton.edibus.enums.JourneyStateEnum;
import com.marton.edibus.events.RideFinishedEvent;
import com.marton.edibus.models.Log;
import com.marton.edibus.models.Stop;
import com.marton.edibus.models.Ride;
import com.marton.edibus.network.BusWebClient;

import java.util.Date;

import de.greenrobot.event.EventBus;

@Singleton
public class JourneyManager {

    @Inject
    private BusWebClient busWebService;

    private EventBus eventBus = EventBus.getDefault();

    // The current ride of the user
    private Ride ride;

    // The stop that is currently under review by the user
    private Stop reviewStop;

    private JourneyStateEnum journeyStateEnum;

    private CurrentActivityEnum currentActivityEnum;

    // The flag indicating whether the journey has been paused
    private boolean paused;

    // The flag indicating whether the journey has been finished
    private boolean finished;

    // The flag indicating whether the journey has been started
    private boolean started;

    // The flag indicating whether the ride should be automated
    private boolean automaticFlow;

    public JourneyManager(){
        this.setDefaults();
    }

    // Set the defaults for the Journey
    public void setDefaults(){
        this.ride = new Ride();
        this.reviewStop = null;
        this.journeyStateEnum = JourneyStateEnum.SETUP_INCOMPLETE;
        this.currentActivityEnum = CurrentActivityEnum.PREPARING;
        this.paused = true;
        this.finished = false;
        this.started = false;
        this.automaticFlow = false;
    }

    public JourneyStateEnum getJourneyStateEnum() {
        return journeyStateEnum;
    }

    public CurrentActivityEnum getCurrentActivityEnum() {
        return currentActivityEnum;
    }

    public Ride getRide() {
        return ride;
    }

    public void setRide(Ride ride) {
        this.ride = ride;
    }

    public Stop getReviewStop() {
        return reviewStop;
    }

    public void setReviewStop(Stop reviewStop) {
        this.reviewStop = reviewStop;
    }

    public boolean getPaused() {
        return paused;
    }

    public boolean getFinished() {
        return finished;
    }

    public boolean getStarted() {
        return started;
    }

    public boolean getAutomaticFlow() {
        return automaticFlow;
    }

    public void setAutomaticFlow(boolean automaticFlow) {
        this.automaticFlow = automaticFlow;
    }

    public void startWaiting(){

        this.journeyStateEnum = JourneyStateEnum.RUNNING;
        this.currentActivityEnum = CurrentActivityEnum.WAITING;

        this.paused = false;
        this.started = true;

        this.ride.setStartTime(new Date());
    }

    public void startTravelling(){

        this.journeyStateEnum = JourneyStateEnum.RUNNING;
        this.currentActivityEnum = CurrentActivityEnum.TRAVELLING;

        this.paused = false;
        this.started = true;

        this.ride.setStartTime(new Date());
    }

    public void finishRide(){

        this.journeyStateEnum = JourneyStateEnum.FINISHED;
        this.finished = true;

        this.eventBus.post(new RideFinishedEvent());
        this.ride.setEndTime(new Date());
    }

    // Returns a flag indicating if a ride has been set up
    public boolean rideSetupComplete(){
        return this.ride.getStartStopId() != 0 && this.ride.getEndStopId() != 0 && this.ride.getServiceId() != 0;
    }

    public void saveRide(WebCallBack<Integer> callback){

        // Read current statistics
        int journeys = StatisticsManager.readJourneysFromSharedPreferences();
        int totalWaitingTime = StatisticsManager.readTotalWaitingTimeFromSharedPreferences();
        int totalTravellingTime = StatisticsManager.readTotalTravellingTimeFromSharedPreferences();
        double totalTravellingDistance = StatisticsManager.readTotalTravellingDistanceFromSharedPreferences();

        // Upload as a new journey, or as an existing one
        if (this.ride.getJourneyId() == 0){
            this.busWebService.uploadNewTrip(this.ride, callback);
            journeys++;
        }else{
            this.busWebService.uploadNewTrip(this.ride.getJourneyId(), this.ride, callback);
        }

        // Store general user statistics locally
        totalWaitingTime += this.ride.getWaitDuration();
        totalTravellingTime += this.ride.getTravelDuration();
        totalTravellingDistance += this.ride.getDistance();

        SharedPreferencesManager.writeString(App.getAppContext(), StatisticsManager.JOURNEYS_KEY, String.valueOf(journeys));
        SharedPreferencesManager.writeString(App.getAppContext(), StatisticsManager.TOTAL_WAITING_TIME_KEY, String.valueOf(totalWaitingTime));
        SharedPreferencesManager.writeString(App.getAppContext(), StatisticsManager.TOTAL_TRAVELLING_TIME_KEY, String.valueOf(totalTravellingTime));
        SharedPreferencesManager.writeString(App.getAppContext(), StatisticsManager.TOTAL_TRAVELLING_DISTANCE_KEY, String.valueOf(totalTravellingDistance));

        // Store diary log locally
        Log log = new Log(ride);
        log.save();
    }
}
