package com.marton.edibus.utilities;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.marton.edibus.WebCallBack;
import com.marton.edibus.enums.JourneyStateEnum;
import com.marton.edibus.models.Stop;
import com.marton.edibus.models.Trip;
import com.marton.edibus.network.BusWebClient;

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

    private JourneyStateEnum journeyState = JourneyStateEnum.SETUP_INCOMPLETE;

    // The flag indicating whether the journey has been paused
    private boolean paused = true;

    // The flag indicating whether the journey has been finished
    private boolean finished = false;

    // The flag indicating whether the journey has been started
    private boolean started = false;

    // The flag indicating whether the trip should be uploaded automatically
    private boolean automaticUpload = false;

    public JourneyManager(){
        trip = new Trip();
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
    }

    public void finishTrip(){
        this.journeyState = JourneyStateEnum.FINISHED;
        this.finished = true;
    }

    // Returns a flag indicating if a trip has been set up
    public boolean tripSetupComplete(){
        return this.trip.getStartStopId() != 0 && this.trip.getEndStopId() != 0 && this.trip.getServiceId() != 0;
    }

    public void uploadTrip(WebCallBack callback){

        WebCallBack<Integer> serviceCallback = new WebCallBack<Integer>() {
            @Override
            public void onSuccess(Integer data) {
                trip = new Trip();
                trip.setJourneyId(data);

                journeyState = JourneyStateEnum.UPLOADED;
            }
        };

        // Upload as a new journey, or as an existing one
        if (this.trip.getJourneyId() == 0){
            busWebService.uploadNewTrip(this.trip, serviceCallback);
        }else{
            busWebService.uploadNewTrip(this.trip.getJourneyId(), this.trip, serviceCallback);
        }
    }
}
