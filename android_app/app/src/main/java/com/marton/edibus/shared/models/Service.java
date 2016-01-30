package com.marton.edibus.shared.models;


import java.util.ArrayList;

public class Service {

    public Service(int id, String name, String type, String description, ArrayList<Stop> stops){
        this.id = id;
        this.name = name;
        this.type = type;
        this.description = description;
        this.stops = stops;
    }

    private int id;

    private String name;

    private String type;

    private String description;

    private ArrayList<Stop> stops;

    public int getId(){
        return this.id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public ArrayList<Stop> getStops() {
        return stops;
    }
}
