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

    private boolean seat;

    private float rating;

    private int peopleWaiting;

    private int peopleBoarding;

    public Log(){
    }

    public Log(Date startTime, Date endTime, String startStopName, String endStopName,
               String serviceName, int waitDuration, int travelDuration, boolean seat,
               float rating, int peopleWaiting, int peopleBoarding){

        this.startTime = startTime;
        this.endTime = endTime;
        this.startStopName = startStopName;
        this.endStopName = endStopName;
        this.serviceName = serviceName;
        this.waitDuration = waitDuration;
        this.travelDuration = travelDuration;
        this.seat = seat;
        this.rating = rating;
        this.peopleWaiting = peopleWaiting;
        this.peopleBoarding = peopleBoarding;
    }

    public Log(Trip trip){
        this.startTime = trip.getStartTime();
        this.endTime = trip.getEndTime();
        this.startStopName = trip.getStartStop().getName();
        this.endStopName = trip.getEndStop().getName();
        this.serviceName = trip.getService().getName();
        this.waitDuration = trip.getWaitDuration();
        this.travelDuration = trip.getTravelDuration();
        this.seat = trip.getSeat();
        this.rating = trip.getRating();
        this.peopleWaiting = trip.getPeopleWaiting();
        this.peopleBoarding = trip.getPeopleBoarding();
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
}
