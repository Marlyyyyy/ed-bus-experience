package com.marton.edibus.events;

import com.marton.edibus.enums.TripActionEnum;

public class TripActionFiredEvent {

    public TripActionFiredEvent(){
    }

    public TripActionFiredEvent(TripActionEnum tripActionEnum){

        this.tripActionEnum = tripActionEnum;
    }

    private TripActionEnum tripActionEnum;

    public TripActionEnum getTripActionEnum() {
        return tripActionEnum;
    }

    public void setTripActionEnum(TripActionEnum tripActionEnum) {
        this.tripActionEnum = tripActionEnum;
    }
}
