package com.marton.edibus.models;


import java.util.ArrayList;
import java.util.Date;

public class Journey {

    public Journey(Date startTime, Date endTime, Date createdAt, ArrayList<Trip> trips){
        this.startTime = startTime;
        this.endTime = endTime;
        this.createdAt = createdAt;
        this.trips = trips;
    }

    private Date startTime;

    private Date endTime;

    private Date createdAt;

    private ArrayList<Trip> trips;

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public ArrayList<Trip> getTrips() {
        return trips;
    }
}
