package com.marton.edibus.events;


import com.marton.edibus.enums.RideStateEnum;

public class TrackerStateUpdatedEvent {

    public TrackerStateUpdatedEvent(){
    }

    public TrackerStateUpdatedEvent(double distanceFromGoal, double distanceFromStart, double currentSpeed, double latitude, double longitude, RideStateEnum rideStateEnum){
        this.distanceFromGoal = distanceFromGoal;
        this.distanceFromStart = distanceFromStart;
        this.currentSpeed = currentSpeed;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    private double latitude;

    private double longitude;

    private double distanceFromGoal;

    private double distanceFromStart;

    private double currentSpeed;

    private double maximumSpeed;

    private double averageSpeed;

    private int waitingTime;

    private int travellingTime;

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

    public double getMaximumSpeed() {
        return maximumSpeed;
    }

    public void setMaximumSpeed(double maximumSpeed) {
        this.maximumSpeed = maximumSpeed;
    }

    public double getAverageSpeed() {
        return averageSpeed;
    }

    public void setAverageSpeed(double averageSpeed) {
        this.averageSpeed = averageSpeed;
    }

    public void copyValuesFrom(TrackerStateUpdatedEvent trackerStateUpdatedEvent){
        this.latitude = trackerStateUpdatedEvent.getLatitude();
        this.longitude = trackerStateUpdatedEvent.getLongitude();
        this.distanceFromGoal = trackerStateUpdatedEvent.getDistanceFromGoal();
        this.distanceFromStart = trackerStateUpdatedEvent.getDistanceFromStart();
        this.currentSpeed = trackerStateUpdatedEvent.getCurrentSpeed();
        this.maximumSpeed = trackerStateUpdatedEvent.getMaximumSpeed();
        this.averageSpeed = trackerStateUpdatedEvent.getAverageSpeed();
        this.waitingTime = trackerStateUpdatedEvent.getWaitingTime();
        this.travellingTime = trackerStateUpdatedEvent.getTravellingTime();
    }
}
