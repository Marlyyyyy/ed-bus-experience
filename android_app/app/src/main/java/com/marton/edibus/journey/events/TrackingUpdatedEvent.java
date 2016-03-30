package com.marton.edibus.journey.events;


import com.marton.edibus.journey.enums.RideStateEnum;

public class TrackingUpdatedEvent {

    public TrackingUpdatedEvent(){
    }

    public TrackingUpdatedEvent(double distanceFromGoal, double distanceFromStart, double currentSpeed, double latitude, double longitude, RideStateEnum rideStateEnum){
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

    public void copyValuesFrom(TrackingUpdatedEvent trackingUpdatedEvent){
        this.latitude = trackingUpdatedEvent.getLatitude();
        this.longitude = trackingUpdatedEvent.getLongitude();
        this.distanceFromGoal = trackingUpdatedEvent.getDistanceFromGoal();
        this.distanceFromStart = trackingUpdatedEvent.getDistanceFromStart();
        this.currentSpeed = trackingUpdatedEvent.getCurrentSpeed();
        this.maximumSpeed = trackingUpdatedEvent.getMaximumSpeed();
        this.averageSpeed = trackingUpdatedEvent.getAverageSpeed();
        this.waitingTime = trackingUpdatedEvent.getWaitingTime();
        this.travellingTime = trackingUpdatedEvent.getTravellingTime();
    }
}
