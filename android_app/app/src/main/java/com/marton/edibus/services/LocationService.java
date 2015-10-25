package com.marton.edibus.services;


import android.app.IntentService;
import android.content.Intent;

import com.marton.edibus.enums.JourneyControlEnum;
import com.marton.edibus.events.JourneyControlEvent;
import com.marton.edibus.events.MessageEvent;

import de.greenrobot.event.EventBus;

public class LocationService extends IntentService{

    private EventBus eventBus = EventBus.getDefault();

    private MessageEvent messageEvent;

    private boolean paused = true;

    private boolean stopped = false;

    /**
     * Creates an IntentService.
     *
     */
    public LocationService(){
        super("LocationService");

        this.messageEvent = new MessageEvent();

        // Register as a subscriber
        eventBus.register(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int counter = 0;
        while(true){
            if (!this.paused){
                messageEvent.message = String.valueOf(counter);
                this.eventBus.post(this.messageEvent);
                counter++;
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (this.stopped){
                break;
            }
        }
    }

    public void onEvent(JourneyControlEvent journeyControlEvent){

        switch(journeyControlEvent.journeyControlEnum){
            case START:
                this.paused = false;
                break;
            case PAUSE:
                this.paused = true;
                break;
        }
    }
}
