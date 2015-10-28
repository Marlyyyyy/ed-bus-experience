package com.marton.edibus.models;


import java.util.ArrayList;

public class Stop {

    public Stop(int id, int stopId, String name, float latitude, float longitude, ArrayList<Service> services, float orientation){
        this.id = id;
        this.stopId = stopId;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.services = services;
        this.orientation = orientation;
    }

    private int id;

    private int stopId;

    private String name;

    private double latitude;

    private double longitude;

    private float orientation;

    private ArrayList<Service> services;

    public double getLongitude() {
        return longitude;
    }

    public int getId() {
        return id;
    }

    public int getStopId() {
        return stopId;
    }

    public double getLatitude() {
        return latitude;
    }

    public ArrayList<Service> getServices() {
        return services;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public float getOrientation() {
        return orientation;
    }

    public void setOrientation(float orientation) {
        this.orientation = orientation;
    }

    public void setServices(ArrayList<Service> services) {
        this.services = services;
    }
}
