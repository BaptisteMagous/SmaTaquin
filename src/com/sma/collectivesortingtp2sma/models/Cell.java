package com.sma.collectivesortingtp2sma.models;

public class Cell {

    protected Agent agent;
    protected Coordinates coordinates;

    public Cell(Coordinates coordinates) {
        this.coordinates = coordinates;
        this.agent = null;
    }
    public Cell(Coordinates coordinates, Agent agent) {
        this.coordinates = coordinates;
        this.agent = agent;
    }

    public Coordinates getCoordinates() {
        return this.coordinates;
    }

    public Agent getAgent() {
        return agent;
    }
    public boolean isFree() {
        return agent == null;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
        this.agent.setCoordinates(this.getCoordinates());
    }

    public Agent popAgent(){
        Agent popedAgent = this.agent;
        popedAgent.setCoordinates(null);
        this.agent = null;
        return popedAgent;
    }

    public boolean isOccupied() {
        return !isFree();
    }
}
