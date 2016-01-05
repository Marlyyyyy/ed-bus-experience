package com.marton.edibus.models;


import java.util.ArrayList;
import java.util.Date;

public class Journey {

    public Journey(Date startTime, Date endTime, Date createdAt, ArrayList<Ride> rides){
        this.startTime = startTime;
        this.endTime = endTime;
        this.createdAt = createdAt;
        this.rides = rides;
    }

    public Journey(){}

    private Date startTime;

    private Date endTime;

    private Date createdAt;

    private ArrayList<Ride> rides;

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public ArrayList<Ride> getRides() {
        return rides;
    }
}
