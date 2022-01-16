package com.sma.collectivesortingtp2sma.models;

import java.beans.PropertyChangeSupport;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

import static java.lang.Math.random;

public class Grid {

    private int width;
    private int height;

    private PropertyChangeSupport observable;

    private Cell[][] grid;
    private final Semaphore available = new Semaphore(1);
    private ConcurrentLinkedQueue<Coordinates> updates;

    public Grid(int width, int height){
        this.width = width;
        this.height = height;
        this.grid = new Cell[width][height];

        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                this.grid[x][y] = new Cell(new Coordinates(x, y));
            }
        }

        this.observable = new PropertyChangeSupport(this);
    }


    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public Cell getCell(Coordinates coordinates) {
        if(coordinates == null) return null;
        if(coordinates.getX() >= this.width) return null;
        if(coordinates.getY() >= this.height) return null;
        if(coordinates.getX() < 0) return null;
        if(coordinates.getY() < 0) return null;
        return this.grid[coordinates.getX()][coordinates.getY()];
    }

    public boolean move(Coordinates from, Coordinates to) throws InterruptedException {
        available.acquire();
        boolean isMoveCorrect =     from != null && this.getCell(from) != null && !this.getCell(from).isFree()
                                 && to   != null && this.getCell(to)   != null &&  this.getCell(to).isFree();

        if(isMoveCorrect)
            this.getCell(to).setAgent(this.getCell(from).popAgent());



        available.release();

        if(isMoveCorrect) {
            setUpdated(from);
            setUpdated(to);
        }

        return isMoveCorrect;
    }

    public boolean insertAgent(Agent agent, Coordinates to) throws InterruptedException {
        available.acquire();
        boolean isMoveCorrect = to != null && this.getCell(to).isFree();

        if(isMoveCorrect)
            this.getCell(to).setAgent(agent);

        available.release();

        if(isMoveCorrect) {
            setUpdated(to);
        }

        return isMoveCorrect;
    }

    public Agent pop(Coordinates from) throws InterruptedException {
        available.acquire();
        Agent agent = this.getCell(from).popAgent();
        available.release();

        setUpdated(from);

        return agent;
    }

    public Coordinates getRandomFreeCell() {
        Coordinates coordinates;
        int tries = 50;
        do{
            coordinates = new Coordinates((int)(random() * this.width), (int)(random() * this.height));
            if(tries-- <= 0) return null;
        }while(!this.getCell(coordinates).isFree());
        return coordinates;
    }

    public Coordinates getRandomFreeCellAround(Coordinates coordinates) {
        return getRandomFreeCellAround(coordinates, 5, (int) (random()*4));
    }

    public Coordinates getRandomFreeCellAround(Coordinates coordinates, int try_left, int direction) {
        if(try_left <= 0) return null;

        Coordinates new_coordinates;
        switch (direction) {
            case 0 -> new_coordinates = new Coordinates(
                    coordinates.getX() + 1,
                    coordinates.getY()
            );
            case 1 -> new_coordinates = new Coordinates(
                    coordinates.getX() - 1,
                    coordinates.getY()
            );
            case 2 -> new_coordinates = new Coordinates(
                    coordinates.getX(),
                    coordinates.getY() + 1
            );
            case 3 -> new_coordinates = new Coordinates(
                    coordinates.getX(),
                    coordinates.getY() - 1
            );
            default -> new_coordinates = new Coordinates(
                    coordinates.getX(),
                    coordinates.getY()
            );
        }
        if(this.getCell(new_coordinates) != null && this.getCell(new_coordinates).isFree())
            return new_coordinates;
        else
            return getRandomFreeCellAround(coordinates, try_left - 1, (direction++)%4);


    }

    public void addObserver(ConcurrentLinkedQueue<Coordinates> updates) {
        this.updates = updates;
    }
    
    private void setUpdated(Coordinates coordinates){
        if(updates != null) updates.add(coordinates);
    }

}
