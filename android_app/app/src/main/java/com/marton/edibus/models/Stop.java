package com.marton.edibus.models;


import java.util.ArrayList;

public class Stop {

    public Stop(int id, int stopId, float latitude, float longitude, ArrayList<Service> services){
        this.id = id;
        this.stopId = stopId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.services = services;
    }

    private int id;

    private int stopId;

    private float latitude;

    private float longitude;

    private ArrayList<Service> services;

    public float getLongitude() {
        return longitude;
    }

    public int getId() {
        return id;
    }

    public int getStopId() {
        return stopId;
    }

    public float getLatitude() {
        return latitude;
    }

    public ArrayList<Service> getServices() {
        return services;
    }
}
