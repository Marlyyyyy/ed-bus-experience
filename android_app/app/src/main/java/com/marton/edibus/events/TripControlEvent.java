package com.marton.edibus.events;


import com.marton.edibus.enums.TripControlEnum;

public class TripControlEvent {

    public final TripControlEnum tripControlEnum;

    public TripControlEvent(TripControlEnum tripControlEnum){
        this.tripControlEnum = tripControlEnum;
    }
}
