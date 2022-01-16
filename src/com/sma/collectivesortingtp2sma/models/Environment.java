package com.sma.collectivesortingtp2sma.models;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Environment{

    private final int gridWidth;
    private final int gridHeight;
    private Grid agentGrid;
    private Grid objectiveGrid;
    private double[][] objectiveImage;

    private final List<Agent> agents;

    private int runningAgent = 0;
    private int misplacedAgent = 0;
    private boolean running = false;
    private boolean slowMode = false;

    private final boolean verbose = false;

    private Simulation simulation;
    static private int neighborhoodToConsiderForEvaluation = 1;

    private Mailbox mailbox;

    //region Constructors, Getters & Setters

    public Environment(int width, int height){
        gridWidth = width;
        gridHeight = height;

        setAgentGrid(new Grid(gridWidth, gridHeight));
        setObjectiveGrid(new Grid(gridWidth, gridHeight));
        objectiveImage = new double[width][height];

        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                objectiveImage[x][y] = -2.0;
            }
        }

        agents = new ArrayList<Agent>();
        mailbox = new Mailbox();
    }

    public void setup(int nbAgent) throws InterruptedException {
        float maxAgent = nbAgent;

        // Add agents
        for(; nbAgent > 0; nbAgent--){
            Agent newAgent = new Agent(getNbAgents());

            newAgent.setHue(-1 + 2*nbAgent/maxAgent);
            newAgent.setVariation(1);

            Coordinates position = agentGrid.getRandomFreeCell();
            newAgent.setCoordinates(position);

            Coordinates objective = objectiveGrid.getRandomFreeCell();

            newAgent.setObjective(objective);
            setObjectiveAt(objective, newAgent.getHue());

            addAgent(newAgent);
        }
    }

    public int getGridWidth(){
        return gridWidth;
    }
    public int getGridHeight(){
        return gridHeight;
    }

    public Grid getAgentGrid(){
        return agentGrid;
    }
    public void setAgentGrid(Grid agentGrid) {
        this.agentGrid = agentGrid;
    }
    public Grid getObjectiveGrid(){
        return objectiveGrid;
    }
    public void setObjectiveGrid(Grid objectiveGrid) {
        this.objectiveGrid = objectiveGrid;
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
        agent.setCoordinates(coordinates);
        addAgent(agent);
    }
    public void addAgent(Agent agent) throws InterruptedException {
        if(agent.getCoordinates() == null)
            agent.setCoordinates(getAgentGrid().getRandomFreeCell());

        if(getAgentGrid().insertAgent(agent, agent.getCoordinates())) {
            agents.add(agent);
            agent.setEnvironment(this);
            agent.setSlowMode(this.slowMode);
        }
        getMailbox().register(agent.getAgentId());
    }

    public int getNbAgents() {
        return agents.size();
    }

    public String toString(){
        String display = ("╔" + "═".repeat(gridWidth) + "╗\n")
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

    public Mailbox getMailbox() {
        return mailbox;
    }

    public boolean isCompleted(){return misplacedAgent <= 0;}

    public int getMisplacedAgent() {
        return misplacedAgent;
    }
    //endregion

    //region Starting and stopping
    synchronized public void start(int maxStep) {
        if(verbose) System.out.println("Starting the environment with " + agents.size() + " agents !");

        misplacedAgent = evaluateMissingAgents();

        agents.forEach(agent -> agent.setSteps(maxStep));

        runningAgent = agents.size();
        agents.forEach(Thread::start);

        running = true;
    }

    public void stop() {
        agents.forEach(Thread::interrupt);
        running = false;

        simulation.environmentStoped();

        if(verbose) System.out.println("Simulation ended !");
    }
    //endregion

    synchronized public void notifyAgentFinish() {
        runningAgent--;

        if(!running) return;

        if(runningAgent == 0) //If all agents are done
            stop();
    }

    public int evaluateEnvironment() {
        int moveLefts = 0;
        for(Agent agent:agents){
            moveLefts += agent.getSteps();
        }

        return moveLefts;
    }

    public int evaluateMissingAgents() {
        int missing = 0;
        for(Agent agent:agents){
            if(!agent.getCoordinates().equals(agent.getObjective())) missing++;
        }

        return missing;
    }

    public double getObjectiveAt(Coordinates coordinates) {
        return objectiveImage[coordinates.getX()][coordinates.getY()];
    }
    public void setObjectiveAt(Coordinates coordinates, double hue) {
        objectiveGrid.getCell(coordinates).setAgent(new Agent(-1));
        objectiveImage[coordinates.getX()][coordinates.getY()] = hue;
    }

    public void updateMisplacedAgents(int i) {
        misplacedAgent += i;

        if(misplacedAgent <= 0) stop();
    }

}
