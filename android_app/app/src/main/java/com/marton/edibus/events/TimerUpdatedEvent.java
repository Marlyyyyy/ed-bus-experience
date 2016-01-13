package com.marton.edibus.events;

public class TimerUpdatedEvent {

    public TimerUpdatedEvent(){}

    public int getWaitingMilliseconds() {
        return waitingMilliseconds;
    }

    public void setWaitingMilliseconds(int waitingMilliseconds) {
        this.waitingMilliseconds = waitingMilliseconds;
    }

    public int getTravellingMilliseconds() {
        return travellingMilliseconds;
    }

    public void setTravellingMilliseconds(int travellingMilliseconds) {
        this.travellingMilliseconds = travellingMilliseconds;
    }

    private int waitingMilliseconds;

    private int travellingMilliseconds;
}
