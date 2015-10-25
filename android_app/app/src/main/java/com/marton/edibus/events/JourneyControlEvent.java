package com.marton.edibus.events;


import com.marton.edibus.enums.JourneyControlEnum;

public class JourneyControlEvent {

    public final JourneyControlEnum journeyControlEnum;

    public JourneyControlEvent(JourneyControlEnum journeyControlEnum){
        this.journeyControlEnum = journeyControlEnum;
    }
}
