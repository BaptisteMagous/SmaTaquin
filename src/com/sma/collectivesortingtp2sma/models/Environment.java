package com.sma.collectivesortingtp2sma.models;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Environment{

    private final int gridWidth;
    private final int gridHeight;
    private Grid agentGrid;
    private double[][] objectiveImage;

    private final List<Agent> agents;

    private int runningAgent = 0;
    private boolean running = false;
    private boolean slowMode = false;

    private final boolean verbose = false;

    private Simulation simulation;
    static private int neighborhoodToConsiderForEvaluation = 1;

    //region Constructors, Getters & Setters

    public Environment(int width, int height){
        gridWidth = width;
        gridHeight = height;

        setAgentGrid(new Grid(gridWidth, gridHeight));
        objectiveImage = new double[width][height];

        for (int y = 0; y < gridWidth; y++) {
            for (int x = 0; x < gridHeight; x++) {
                objectiveImage[x][y] = -2.0;
            }
        }

        agents = new ArrayList<Agent>();
    }

    public void setup(int nbAgent) throws InterruptedException {
        // Add agents
        for(; nbAgent > 0; nbAgent--){
            Agent newAgent = new Agent(getNbAgents());
            newAgent.setHue(Math.random() * 2 - 1);
            newAgent.setVariation(1);
            Coordinates objective = agentGrid.getRandomFreeCell();
            newAgent.setObjective(objective);
            setObjectiveAt(objective, newAgent.getHue());
            addAgent(newAgent, objective);
        }
    }
    public Grid getAgentGrid(){
        return agentGrid;
    }

    public int getGridWidth(){
        return gridWidth;
    }
    public int getGridHeight(){
        return gridHeight;
    }

    public void setAgentGrid(Grid agentGrid) {
        this.agentGrid = agentGrid;
    }

    public void setSlowMode(boolean slow) {
        slowMode = slow;
        agents.forEach(agent -> agent.setSlowMode(slow));
    }

    public void setSimulation(Simulation simulation){
        this.simulation = simulation;
    }

    public void setUpdateQueue(ConcurrentLinkedQueue<Coordinates> updates) {
        if(this.agentGrid != null) this.agentGrid.addObserver(updates);
    }

    public void addAgent(Agent agent, Coordinates coordinates) throws InterruptedException {
        if(getAgentGrid().insertAgent(agent, coordinates)) {
            agents.add(agent);
            agent.setEnvironment(this);
            agent.setSlowMode(this.slowMode);
        }
    }
    public void addAgent(Agent agent) throws InterruptedException {
        addAgent(agent, getAgentGrid().getRandomFreeCell());
    }

    public int getNbAgents() {
        return agents.size();
    }

    public String toString(){
        String display =
                ("╔" + "═".repeat(gridWidth) + "╗\n")
                        + ("║" + " ".repeat(gridWidth) + "║\n").repeat(gridHeight)
                        + ("╚" + "═".repeat(gridWidth) + "╝\n");

        int lineLength = 3 + gridWidth;
        StringBuilder displayBuilder = new StringBuilder(display);

        for (Agent agent : agents)
            displayBuilder.setCharAt(
                    lineLength // first line
                            + agent.getCoordinates().getY() * lineLength
                            + agent.getCoordinates().getX() + 1,
                    (char) agent.getAgentId()
            );

        return displayBuilder.toString();
    }
    //endregion

    //region Starting and stopping
    synchronized public void start(int maxStep) {
        if(verbose) System.out.println("Starting the environment with " + agents.size() + " agents !");
        agents.forEach(agent -> agent.setSteps(maxStep));

        runningAgent = agents.size();
        agents.forEach(Thread::start);

        running = true;
    }

    public void stop() {
        agents.forEach(Thread::interrupt);
        if(verbose) System.out.println("Stopped the environment.");
    }
    //endregion

    synchronized public void notifyAgentFinish() {
        runningAgent--;

        if(!running) return;

        if(runningAgent == 0) { //If all agents are done
            running = false;

            simulation.environmentStoped();

            if(verbose) System.out.println("Simulation ended !");
        }
    }

    public int evaluateEnvironment() {
        return 10;
    }

    public double getObjectiveAt(Coordinates coordinates) {
        return objectiveImage[coordinates.getX()][coordinates.getY()];
    }
    public void setObjectiveAt(Coordinates coordinates, double hue) {
        objectiveImage[coordinates.getX()][coordinates.getY()] = hue;
    }
}
