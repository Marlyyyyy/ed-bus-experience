package com.marton.edibus.utilities;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.marton.edibus.App;
import com.marton.edibus.WebCallBack;
import com.marton.edibus.enums.JourneyStateEnum;
import com.marton.edibus.models.Log;
import com.marton.edibus.models.Stop;
import com.marton.edibus.models.Trip;
import com.marton.edibus.network.BusWebClient;

import java.util.Date;

import de.greenrobot.event.EventBus;

@Singleton
public class JourneyManager {

    @Inject
    BusWebClient busWebService;

    private EventBus eventBus = EventBus.getDefault();

    // The current trip of the user
    private Trip trip;

    // The stop that is currently under review by the user
    private Stop reviewStop;

    private JourneyStateEnum journeyState;

    // The flag indicating whether the journey has been paused
    private boolean paused;

    // The flag indicating whether the journey has been finished
    private boolean finished;

    // The flag indicating whether the journey has been started
    private boolean started;

    // The flag indicating whether the trip should be uploaded automatically
    private boolean automaticUpload;

    public JourneyManager(){
        this.setDefaults();
    }

    // Set the defaults for the Journey
    public void setDefaults(){
        this.trip = new Trip();
        this.reviewStop = null;
        this.journeyState = JourneyStateEnum.SETUP_INCOMPLETE;
        this.paused = true;
        this.finished = false;
        this.started = false;
        this.automaticUpload = false;
    }

    public JourneyStateEnum getJourneyState() {
        return journeyState;
    }

    public Trip getTrip() {
        return trip;
    }

    public void setTrip(Trip trip) {
        this.trip = trip;
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

    public boolean getAutomaticUpload() {
        return automaticUpload;
    }

    public void setAutomaticUpload(boolean automaticUpload) {
        this.automaticUpload = automaticUpload;
    }

    public void pauseTrip(){
        this.journeyState = JourneyStateEnum.PAUSED;
        this.paused = true;
    }

    public void continueTrip(){
        this.journeyState = JourneyStateEnum.RUNNING;
        this.paused = false;
    }

    public void startTrip(){
        this.journeyState = JourneyStateEnum.RUNNING;
        this.paused = false;
        this.started = true;

        this.trip.setStartTime(new Date());
    }

    public void finishTrip(){
        this.journeyState = JourneyStateEnum.FINISHED;
        this.finished = true;

        this.trip.setEndTime(new Date());
    }

    // Returns a flag indicating if a trip has been set up
    public boolean tripSetupComplete(){
        return this.trip.getStartStopId() != 0 && this.trip.getEndStopId() != 0 && this.trip.getServiceId() != 0;
    }

    public void saveTrip(WebCallBack<Integer> callback){

        // Read current statistics
        int journeys = StatisticsManager.readJourneysFromSharedPreferences();
        int totalWaitingTime = StatisticsManager.readTotalWaitingTimeFromSharedPreferences();
        int totalTravellingTime = StatisticsManager.readTotalTravellingTimeFromSharedPreferences();

        // Upload as a new journey, or as an existing one
        if (this.trip.getJourneyId() == 0){
            this.busWebService.uploadNewTrip(this.trip, callback);
            journeys++;
        }else{
            this.busWebService.uploadNewTrip(this.trip.getJourneyId(), this.trip, callback);
        }

        // Store general user statistics locally
        totalWaitingTime += this.trip.getWaitDuration();
        totalTravellingTime += this.trip.getTravelDuration();

        SharedPreferencesManager.writeString(App.getAppContext(), StatisticsManager.JOURNEYS_KEY, String.valueOf(journeys));
        SharedPreferencesManager.writeString(App.getAppContext(), StatisticsManager.TOTAL_WAITING_TIME_KEY, String.valueOf(totalWaitingTime));
        SharedPreferencesManager.writeString(App.getAppContext(), StatisticsManager.TOTAL_TRAVELLING_TIME_KEY, String.valueOf(totalTravellingTime));
        SharedPreferencesManager.writeString(App.getAppContext(), StatisticsManager.TOTAL_TRAVELLING_DISTANCE_KEY, String.valueOf(this.trip.getDistance()));

        // Store diary log locally
        Log log = new Log(trip);
        log.save();
    }
}
