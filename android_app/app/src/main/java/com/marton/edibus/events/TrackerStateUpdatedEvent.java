package com.marton.edibus.events;


public class TrackerStateUpdatedEvent {

    public TrackerStateUpdatedEvent(){
    }

    public TrackerStateUpdatedEvent(double distanceFromGoal, double distanceFromStart, double currentSpeed){
        this.distanceFromGoal = distanceFromGoal;
        this.distanceFromStart = distanceFromStart;
        this.currentSpeed = currentSpeed;
    }

    private double distanceFromGoal;

    private double distanceFromStart;

    private double currentSpeed;

    public double getDistanceFromGoal() {
        return distanceFromGoal;
    }

    public void setDistanceFromGoal(double distanceFromGoal) {
        this.distanceFromGoal = distanceFromGoal;
    }

    public double getDistanceFromStart() {
        return distanceFromStart;
    }

    public void setDistanceFromStart(double distanceFromStart) {
        this.distanceFromStart = distanceFromStart;
    }

    public double getCurrentSpeed() {
        return currentSpeed;
    }

    public void setCurrentSpeed(double currentSpeed) {
        this.currentSpeed = currentSpeed;
    }
}
