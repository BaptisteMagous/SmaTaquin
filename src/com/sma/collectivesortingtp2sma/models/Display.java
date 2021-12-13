package com.sma.collectivesortingtp2sma.models;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Display extends Thread{
    private Environment environment;
    private Stage stage = null;

    private GridPane grid;
    private ImageView[][] gridImages;
    private int heigh, width;

    private boolean running = false;

    private Image imgAgent = new Image("file:img/agent.png", 32, 32, false, false);
    private Image imgEmpty = new Image("file:img/empty.png", 32, 32, false, false);
    private ColorAdjust color_adjust = new ColorAdjust();
    private ConcurrentLinkedQueue<Coordinates> updates;
    private boolean verbose = false;

    public Display(){
    }

    public Display(Environment environment, Stage stage){
        setEnvironment(environment);
        setStage(stage);
    }

    public Display(Environment environment){
        setEnvironment(environment);
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;

        this.running = true;

        // Setup the stage to fullscreen (margin 100px)
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();
        this.stage.setX(bounds.getMinX()+100);
        this.stage.setY(bounds.getMinY()+100);
        this.stage.setWidth(bounds.getWidth()-200);
        this.stage.setHeight(bounds.getHeight()-200);

        // Set the name
        this.stage.setTitle("Simulation de tri multi-agents");

        // Setup the root borderpane
        BorderPane root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(Color.rgb(199, 250, 230), CornerRadii.EMPTY, Insets.EMPTY)));

        //Setup the center
        root.setCenter(grid);
        grid.setAlignment(Pos.CENTER);

        // Display the grid
        grid.requestFocus();
        this.updateGrid();

        // Display scene
        this.stage.setScene(new Scene(root, 400, 400));
        this.stage.show();
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.grid = new GridPane();
        this.environment = environment;
        this.width = this.environment.getGridWidth();
        this.heigh = this.environment.getGridHeigh();

        gridImages = new ImageView[width][heigh];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < heigh; y++) {
                ImageView imageView = new ImageView();
                gridImages[x][y] = imageView;
                grid.add(imageView, x, y);
            }
        }
    }

    private void updateGrid(){
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < heigh; y++) {
                updateGridCell(new Coordinates(x, y));
            }
        }
    }

    public void setUpdateQueue(ConcurrentLinkedQueue<Coordinates> updates) {
        this.updates = updates;
    }

    public void run(){
        init();
        while (!isInterrupted()){
            try{
                execute();
            }catch (InterruptedException e){
                Thread.currentThread().interrupt();
            }
        }
        terminate();
    }

    protected void init() {
        if(verbose) System.out.println("Display started");
        updateGrid();
    }

    protected void execute() throws InterruptedException {
        if(updates.isEmpty()) return;

        updateGridCell(updates.poll());
    }

    private void updateGridCell(Coordinates coordinates) {
        if(!running) return;

        Image newImage = imgEmpty;

        if (getEnvironment().getAgentGrid().getCell(coordinates).isOccupied()){
            Agent agent = getEnvironment().getAgentGrid().getCell(coordinates).getAgent();
            newImage = imgAgent;
            color_adjust.setHue(agent.getHue());
        }else{
            newImage = imgEmpty;
        }

        gridImages[coordinates.getX()][coordinates.getY()].setImage(newImage);
        gridImages[coordinates.getX()][coordinates.getY()].setEffect(color_adjust);

    }

    protected void terminate() {
        if(verbose) System.out.println("Display stopped");
    }
}
