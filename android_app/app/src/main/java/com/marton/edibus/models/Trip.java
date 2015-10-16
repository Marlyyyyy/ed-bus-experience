package com.marton.edibus.models;


import java.util.Date;

public class Trip {

    /**
     * Description
     * @param id
     * @param startTime
     * @param endTime
     * @param journey
     * @param startStop
     * @param endStop
     * @param service
     * @param waitDuration
     * @param travelDuration
     * @param seat
     * @param rating
     * @param createdAt
     * @param updatedAt
     */
    public Trip(int id, Date startTime, Date endTime, Journey journey, Stop startStop, Stop endStop,
            Service service, int waitDuration, int travelDuration, boolean seat, float rating,
            Date createdAt, Date updatedAt){

        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.journey = journey;
        this.startStop = startStop;
        this.endStop = endStop;
        this.service = service;
        this.waitDuration = waitDuration;
        this.travelDuration = travelDuration;
        this.seat = seat;
        this.rating = rating;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;

    }

    public Trip(){}

    private int id;

    private Date startTime;

    private Date endTime;

    private Journey journey;

    private int journeyId;

    private Stop startStop;

    private int startStopId;

    private Stop endStop;

    private int endStopId;

    private Service service;

    private int serviceId;

    private int waitDuration;

    private int travelDuration;

    private boolean seat;

    private float rating;

    private Date createdAt;

    private Date updatedAt;

    public int getId(){
        return id;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public Journey getJourney() {
        return journey;
    }

    public Stop getStartStop() {
        return startStop;
    }

    public Stop getEndStop() {
        return endStop;
    }

    public Service getService() {
        return service;
    }

    public int getWaitDuration() {
        return waitDuration;
    }

    public int getTravelDuration() {
        return travelDuration;
    }

    public int getJourneyId() {
        return journeyId;
    }

    public void setJourneyId(int journeyId) {
        this.journeyId = journeyId;
    }

    public int getStartStopId() {
        return startStopId;
    }

    public void setStartStopId(int startStopId) {
        this.startStopId = startStopId;
    }

    public int getEndStopId() {
        return endStopId;
    }

    public void setEndStopId(int endStopId) {
        this.endStopId = endStopId;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public boolean isSeat() {
        return seat;
    }

    public float getRating() {
        return rating;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public void setWaitDuration(int waitDuration) {
        this.waitDuration = waitDuration;
    }

    public void setTravelDuration(int travelDuration) {
        this.travelDuration = travelDuration;
    }

    public void setSeat(boolean seat) {
        this.seat = seat;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }
}
