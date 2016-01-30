package com.marton.edibus.journey.events;

import com.marton.edibus.journey.enums.RideActionEnum;

public class RideActionFiredEvent {

    public RideActionFiredEvent(){
    }

    public RideActionFiredEvent(RideActionEnum rideActionEnum){

        this.rideActionEnum = rideActionEnum;
    }

    private RideActionEnum rideActionEnum;

    public RideActionEnum getRideActionEnum() {
        return rideActionEnum;
    }

    public void setRideActionEnum(RideActionEnum rideActionEnum) {
        this.rideActionEnum = rideActionEnum;
    }
}
