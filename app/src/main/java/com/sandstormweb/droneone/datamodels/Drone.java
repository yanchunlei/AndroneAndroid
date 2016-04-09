package com.sandstormweb.droneone.datamodels;

public class Drone
{
    private String name;
    private long id;

    public Drone(String name, long id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public long getId() {
        return id;
    }
}
