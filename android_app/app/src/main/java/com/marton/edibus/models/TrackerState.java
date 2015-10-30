package com.marton.edibus.models;


public class TrackerState {

    public TrackerState(){
    }

    public TrackerState(double currentLatitude, double currentLongitude, double distanceFromGoal, double distanceFromStart, double currentSpeed){
        this.latitude = currentLatitude;
        this.longitude = currentLongitude;
        this.distanceFromGoal = distanceFromGoal;
        this.distanceFromStart = distanceFromStart;
        this.currentSpeed = currentSpeed;
    }

    private double latitude;

    private double longitude;

    private double distanceFromGoal;

    private double distanceFromStart;

    private double currentSpeed;

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
