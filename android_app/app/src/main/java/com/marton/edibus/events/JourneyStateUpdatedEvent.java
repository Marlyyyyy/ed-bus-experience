package com.marton.edibus.events;


import com.marton.edibus.enums.JourneyStateEnum;

public class JourneyStateUpdatedEvent {

    private JourneyStateEnum journeyStateEnum;

    public JourneyStateUpdatedEvent(){
    }

    public JourneyStateUpdatedEvent(JourneyStateEnum journeyStateEnum){
        this.journeyStateEnum = journeyStateEnum;
    }

    public JourneyStateEnum getJourneyStateEnum() {
        return journeyStateEnum;
    }

    public void setJourneyStateEnum(JourneyStateEnum journeyStateEnum) {
        this.journeyStateEnum = journeyStateEnum;
    }
}
