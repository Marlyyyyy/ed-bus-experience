package com.marton.edibus.events;


import com.marton.edibus.enums.CurrentActivityEnum;

public class TrackerStateUpdatedEvent {

    public TrackerStateUpdatedEvent(){
    }

    public TrackerStateUpdatedEvent(double distanceFromGoal, double distanceFromStart, double currentSpeed, double latitude, double longitude, CurrentActivityEnum currentActivityEnum){
        this.distanceFromGoal = distanceFromGoal;
        this.distanceFromStart = distanceFromStart;
        this.currentSpeed = currentSpeed;
        this.latitude = latitude;
        this.longitude = longitude;
        this.currentActivityEnum = currentActivityEnum;
    }

    private double latitude;

    private double longitude;

    private double distanceFromGoal;

    private double distanceFromStart;

    private double currentSpeed;

    private int waitingTime;

    private int travellingTime;

    private CurrentActivityEnum currentActivityEnum;

    public CurrentActivityEnum getCurrentActivityEnum() {
        return currentActivityEnum;
    }

    public void setCurrentActivityEnum(CurrentActivityEnum currentActivityEnum) {
        this.currentActivityEnum = currentActivityEnum;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getWaitingTime() {
        return waitingTime;
    }

    public void setWaitingTime(int waitingTime) {
        this.waitingTime = waitingTime;
    }

    public int getTravellingTime() {
        return travellingTime;
    }

    public void setTravellingTime(int travellingTime) {
        this.travellingTime = travellingTime;
    }

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
