package com.marton.edibus.events;


import com.marton.edibus.enums.CurrentActivityEnum;

public class CurrentActivityUpdatedEvent {

    private CurrentActivityEnum currentActivityEnum;

    public CurrentActivityUpdatedEvent(){
    }

    public CurrentActivityUpdatedEvent(CurrentActivityEnum currentActivityEnum){
        this.currentActivityEnum = currentActivityEnum;
    }

    public CurrentActivityEnum getCurrentActivityEnum() {
        return currentActivityEnum;
    }

    public void setCurrentActivityEnum(CurrentActivityEnum currentActivityEnum) {
        this.currentActivityEnum = currentActivityEnum;
    }
}
