package com.sma.collectivesortingtp2sma.models;

public class Mail {
    private Integer from, to;
    private Action action;
    private Coordinates coordinates;

    public Mail(Integer from, Integer to, Action action, Coordinates coordinates) {
        this.from = from;
        this.to = to;
        this.action = action;
        this.coordinates = coordinates;
    }

    public Integer getFrom() {
        return from;
    }

    public Integer getTo() {
        return to;
    }

    public Action getAction() {
        return action;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public String toString(){
        return "Mail [" + from + ">" +  to + " - " + action + " - " + coordinates + "]";
    }
}
