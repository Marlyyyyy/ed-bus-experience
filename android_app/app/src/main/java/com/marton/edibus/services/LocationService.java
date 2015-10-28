package com.marton.edibus.services;


import android.content.Intent;

import com.google.inject.Inject;
import com.marton.edibus.events.MessageEvent;
import com.marton.edibus.utilities.JourneyManager;

import de.greenrobot.event.EventBus;
import roboguice.service.RoboIntentService;

public class LocationService extends RoboIntentService {

    private EventBus eventBus = EventBus.getDefault();

    @Inject private JourneyManager journeyManager;

    private MessageEvent messageEvent;

    /**
     * Creates an IntentService.
     *
     */
    public LocationService(){
        super("LocationService");

        this.messageEvent = new MessageEvent();
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
                    messageEvent.message = String.valueOf(counter);
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

    // TODO: let this use the latitude and the longitude of the user's current location
    private boolean shouldTripFinish(int counter){
        return counter > 20;
    }
}
