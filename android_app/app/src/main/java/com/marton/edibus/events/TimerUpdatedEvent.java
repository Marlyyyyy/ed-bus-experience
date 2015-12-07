package com.marton.edibus.events;

public class TimerUpdatedEvent {
    public TimerUpdatedEvent(){};

    public int getWaitingSeconds() {
        return waitingSeconds;
    }

    public void setWaitingSeconds(int waitingSeconds) {
        this.waitingSeconds = waitingSeconds;
    }

    public int getTravellingSeconds() {
        return travellingSeconds;
    }

    public void setTravellingSeconds(int travellingSeconds) {
        this.travellingSeconds = travellingSeconds;
    }

    private int waitingSeconds;

    private int travellingSeconds;
}
