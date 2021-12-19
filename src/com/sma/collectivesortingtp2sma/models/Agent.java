package com.sma.collectivesortingtp2sma.models;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import static java.lang.Math.pow;
import static java.lang.Math.random;

public class Agent extends Thread{
    protected Coordinates coordinates = null;
    private Environment environment;

    private double hue = 0.0;
    private int variation = 1;
    private int id;
    private Coordinates objective;

    private boolean verbose = false;

    private Queue<Integer> lastVisited = new LinkedList<Integer>();

    private int steps = 0;
    private boolean slowMode = true;
    private float slowModeSpeed = 400;

    public Agent(int id){
        this.id = id;
        this.coordinates = null;
    }
    public Agent(int id, Coordinates coordinates){
        this.id = id;
        this.coordinates = coordinates;
    }

    // region Getters & Setters
    public int getAgentId() {
        return id;
    }

    public void setAgentId(int id) {
        this.id = id;
    }


    public Coordinates getCoordinates() {
        return this.coordinates;
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


    public double getHue() {
        return hue;
    }

    public void setHue(double hue) {
        this.hue = hue;
    }


    public int getVariation() {
        return variation;
    }

    public void setVariation(int variation) {
        this.variation = variation;
    }


    public void setSlowMode(boolean slow){
        slowMode = slow;
    }

    public void setSteps(int steps){
        this.steps = steps;
    }

    // endregion

    //region Thread Run
    public void run(){
        init();
        /*if (slowMode) {
            try {
                Thread.sleep((long) (2000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }*/
        while (!isInterrupted() && steps-- > 0){
            try{
                execute();

                //Wait 75% to 125% of slowModeSpeed
                if(slowMode) Thread.sleep((long) (slowModeSpeed * (0.75 + 0.5*random())));
            }catch (InterruptedException e){
                Thread.currentThread().interrupt();
            }
        }
        terminate();
    }

    protected void init() {
        if(verbose) System.out.println("BOT started");
    }

    protected void terminate() {
        if(verbose) System.out.println("BOT stopped");
        getEnvironment().notifyAgentFinish();
    }
    //endregion

    //region Actions
    protected void execute() throws InterruptedException {
        if(verbose) System.out.println("BOT try moving");

        getEnvironment().getAgentGrid().move(
                this.getCoordinates(),
                this.getEnvironment().getAgentGrid().getRandomFreeCellAround(this.coordinates)
        );

    }

    public void setObjective(Coordinates objective) {
        this.objective = objective;
    }

    /*private void analyseSurrondings(){
        if(!analyseSurrondings) {
            visitCell(getEnvironment().getResourceGrid().getCell(getCoordinates()));
            return;
        }
        for(int x = getCoordinates().getX()-vision; x <= getCoordinates().getX()+vision; x++)
            for(int y = getCoordinates().getY()-vision; y <= getCoordinates().getY()+vision; y++)
                visitCell(getEnvironment().getResourceGrid().getCell(new Coordinates(x, y)));
    }

    private void visitCell(Cell cell){
        // Add the cell in the list of last visited cell
        if(cell == null) lastVisited.add(0);
        else {
            int type = 0;
            if(!cell.isFree())
                type = ((Object) cell.getElement()).getType();

            if(random() < error) switch (type) { //Switch type on error
                case 1 -> type = 2;
                case 2 -> type = 1;
                case 0 -> type = 0; //No false positive, but it can be a good idea to have the agent mistakin an empty cell for an object
            }

            if(verbose) System.out.println("Visiting " + type + " (" + f.get(type) + " will be remplaced with " + (f.get(type) + (float)1/memorySize) + ")");

            if(!f.containsKey(type)) f.put(type, 0f);

            f.put(type, f.get(type) + (float)1/memorySize);
            lastVisited.add(type);
        }

        // Remove elements if the queue is too big
        if(lastVisited.size() > memorySize) {
            try{
                int type = lastVisited.poll();
                f.put(type, f.get(type) - (float)1/memorySize);
            }catch (NullPointerException error){
                System.err.println(error);
            }
        }
    }*/

    //endregion
}
