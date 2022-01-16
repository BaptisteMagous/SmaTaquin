package com.sma.collectivesortingtp2sma.models;

import java.util.LinkedList;
import java.util.Queue;

import static java.lang.Math.random;

public class Agent extends Thread{
    //static Agent fakeAgent = new Agent(-1);
    protected Coordinates coordinates = null;
    private Environment environment;

    private int id;
    private double hue = 0.0;
    private int variation = 1;

    private Coordinates objective;

    private IAType ia = IAType.RandomAround;
    private int vision = 2;

    private boolean verbose = false;

    private Queue<Integer> lastVisited = new LinkedList<Integer>();

    private int steps = 0;
    private boolean slowMode = false;
    private float slowModeSpeed = 400;

    public Agent(int id){
        this.id = id;
        this.coordinates = null;
    }
    public Agent(int id, Coordinates coordinates){
        this.id = id;
        this.coordinates = coordinates;
    }
    public Agent(int id, Coordinates coordinates, IAType ia){
        this.id = id;
        this.coordinates = coordinates;
        this.ia = ia;
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

    public Coordinates getObjective() {
        return this.objective;
    }

    public void setObjective(Coordinates objective) {
        this.objective = objective;
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

    public int getSteps(){
        return steps;
    }
    public void setSteps(int steps){
        this.steps = steps;
    }

    // endregion

    //region Thread Run
    public void run(){
        try {
            init();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

    protected void init() throws InterruptedException {
        if(verbose) System.out.println("BOT started");
        if(slowMode) Thread.sleep((long) 8000);
    }

    protected void terminate() {
        if(verbose) System.out.println("BOT stopped");
        getEnvironment().notifyAgentFinish();
    }
    //endregion

    //region Actions
    protected void execute() throws InterruptedException { 
        if(verbose) System.out.println("BOT try moving");

        // Select a coordinates to move to
        Coordinates moveFrom = getCoordinates();
        Coordinates moveTo = null;

        switch (ia){
            case Random:
                // Random agent just select a random free cell around them (Strict Melee)
                moveTo = this.getEnvironment().getAgentGrid().getRandomFreeCellAround(moveFrom);
                break;

            // Straight and RandomAround act similarly
            // They advance toward their objective. They pick randomly between the X and Y axis,
            // and move 1 space along that axis toward their destination (axis are weighted by the distance).
            // RandomAround only differs by the fact the agent will randomly move randomly. If it is far away from
            // his destination, it will tend to be more random, and as it approach his objective, it will be more likely
            // to act like Straight
            case Straight:
            case RandomAround:

                // Decide whether to advance or move randomly based on the distance
                boolean advance;
                double distance = Coordinates.getDistance(moveFrom, objective);
                if(distance > 0.5){
                    if (ia == IAType.Straight) advance = true; // Straight IA always advance
                    else                       advance = Math.random() < vision/distance;

                    // Advance Straight
                    if(advance){
                        // Decide along which axis to move on
                        boolean moveX = Math.random() < Coordinates.getXDistance(moveFrom, objective)/distance;

                        moveTo = new Coordinates(moveFrom);
                        if(moveX) moveTo.addX(Coordinates.getXDirection(moveFrom, objective));
                        if(!moveX) moveTo.addY(Coordinates.getYDirection(moveFrom, objective));
                    }

                    // Advance Randomly
                    else
                        // Select a random free cell around it (Strict Melee)
                        moveTo = this.getEnvironment().getAgentGrid().getRandomFreeCellAround(moveFrom);

                }
                break;
        }


        // Retrieve all mails and check his destination isn't a a cell that is reserved by another agent
        Mail[] mails = environment.getMailbox().retrieve(getAgentId());
        boolean canMoveTo = true;
        boolean mustMove = false;
        for (Mail mail:mails) {
            if(mail == null) continue;
            //Don't go on a cell that is requested by another agent
            if(canMoveTo
                    && mail.getAction() == Action.CLEAR_POSITION
                    && mail.getCoordinates().equals(moveTo)) canMoveTo = false;

            //Move if another agent request free space on his cell
            if (!mustMove
                    && mail.getAction() == Action.CLEAR_POSITION
                    && mail.getCoordinates().equals(moveFrom)) mustMove = true;
        }



        boolean hasMoved = canMoveTo && getEnvironment().getAgentGrid().move(moveFrom, moveTo);

        if(!hasMoved && mustMove) { //If we weren't able to move but another agent asked this agent to move
            //Try to leave the cell in any possible way
            moveTo = this.getEnvironment().getAgentGrid().getRandomFreeCellAround(moveFrom);
            hasMoved = getEnvironment().getAgentGrid().move(moveFrom, moveTo);
        }

        if(!hasMoved) { // If an agent was blocking the way, send it a mail to ask to move away
            Cell destination = getEnvironment().getAgentGrid().getCell(moveTo);
            if(destination != null && destination.getAgent() != null){
                getEnvironment().getMailbox().send(getAgentId(), destination.getAgent().getAgentId(), Action.CLEAR_POSITION, moveTo);
            }
        }

        if(hasMoved){
            if(moveFrom.equals(getObjective()))
                getEnvironment().updateMisplacedAgents(1);
            if(moveTo.equals(getObjective()))
                getEnvironment().updateMisplacedAgents(-1);
        }
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
