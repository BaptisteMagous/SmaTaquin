package com.sma.collectivesortingtp2sma.models;

public class Object {

    private Coordinates coordinates;
    private Environment environment;
    private int type;

    public Object(int type){
        this.coordinates = null;
        this.type = type;
    }
    public Object(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public Object(Coordinates coordinates, int type) {
        this.coordinates = coordinates;
        this.type = type;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }


    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public int getType() {
        return type;
    }
}
