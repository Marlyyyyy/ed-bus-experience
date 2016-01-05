package com.marton.edibus.models;


import com.orm.SugarRecord;

import java.util.Date;

public class Log extends SugarRecord {

    private Date startTime;

    private Date endTime;

    private String startStopName;

    private String endStopName;

    private String serviceName;

    private int waitDuration;

    private int travelDuration;

    private double distance;

    private double averageSpeed;

    private boolean seat;

    private boolean greet;

    private float rating;

    private int peopleWaiting;

    private int peopleBoarding;

    public Log(){
    }

    public Log(Date startTime, Date endTime, String startStopName, String endStopName,
               String serviceName, int waitDuration, int travelDuration, double distance, double averageSpeed,
               boolean seat, boolean greet, float rating, int peopleWaiting, int peopleBoarding){

        this.startTime = startTime;
        this.endTime = endTime;
        this.startStopName = startStopName;
        this.endStopName = endStopName;
        this.serviceName = serviceName;
        this.waitDuration = waitDuration;
        this.travelDuration = travelDuration;
        this.distance = distance;
        this.averageSpeed = averageSpeed;
        this.seat = seat;
        this.greet = greet;
        this.rating = rating;
        this.peopleWaiting = peopleWaiting;
        this.peopleBoarding = peopleBoarding;
    }

    public Log(Ride ride){
        this.startTime = ride.getStartTime();
        this.endTime = ride.getEndTime();
        this.startStopName = ride.getStartStop().getName();
        this.endStopName = ride.getEndStop().getName();
        this.serviceName = ride.getService().getName();
        this.waitDuration = ride.getWaitDuration();
        this.travelDuration = ride.getTravelDuration();
        this.distance = ride.getDistance();
        this.averageSpeed = 1000 * this.distance / (this.waitDuration + this.travelDuration);
        this.seat = ride.getSeat();
        this.greet = ride.getGreet();
        this.rating = ride.getRating();
        this.peopleWaiting = ride.getPeopleWaiting();
        this.peopleBoarding = ride.getPeopleBoarding();
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getStartStopName() {
        return startStopName;
    }

    public void setStartStopName(String startStopName) {
        this.startStopName = startStopName;
    }

    public String getEndStopName() {
        return endStopName;
    }

    public void setEndStopName(String endStopName) {
        this.endStopName = endStopName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public int getWaitDuration() {
        return waitDuration;
    }

    public void setWaitDuration(int waitDuration) {
        this.waitDuration = waitDuration;
    }

    public int getTravelDuration() {
        return travelDuration;
    }

    public void setTravelDuration(int travelDuration) {
        this.travelDuration = travelDuration;
    }

    public boolean isSeat() {
        return seat;
    }

    public void setSeat(boolean seat) {
        this.seat = seat;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public int getPeopleWaiting() {
        return peopleWaiting;
    }

    public void setPeopleWaiting(int peopleWaiting) {
        this.peopleWaiting = peopleWaiting;
    }

    public int getPeopleBoarding() {
        return peopleBoarding;
    }

    public void setPeopleBoarding(int peopleBoarding) {
        this.peopleBoarding = peopleBoarding;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getAverageSpeed() {
        return averageSpeed;
    }

    public void setAverageSpeed(double averageSpeed) {
        this.averageSpeed = averageSpeed;
    }

    public boolean isGreet() {
        return greet;
    }

    public void setGreet(boolean greet) {
        this.greet = greet;
    }
}
