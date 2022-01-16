package com.sma.collectivesortingtp2sma;

import com.sma.collectivesortingtp2sma.models.Environment;
import com.sma.collectivesortingtp2sma.models.Simulation;
import javafx.application.Application;
import javafx.stage.Stage;

public class mainAnalyse extends Application {
    @Override
    public void start(Stage primaryStage) throws InterruptedException {
        int nbTestPerSetings = 10;

        for (int size = 5; size <= 10; size += 1) {
            for (float agentOccupation = 0.5f; agentOccupation <= 0.95f; agentOccupation += 0.2f)
                for (int steps = 16000; steps < 16001; steps *= 2)
                    for (int iteration = 0; iteration < nbTestPerSetings; iteration++) {
                        // Creating the environment
                        Environment environment = new Environment(size, size);
                        environment.setup((int) (size * size * agentOccupation));

                        // Create simulation WITHOUT linking it to the stage (headless mode)
                        Simulation simulation = new Simulation(environment);

                        // Run simulation
                        simulation.run(steps);
                    }
        }


        primaryStage.close();

    }

    public static void main(String[] args) {
        launch(args);
    }
}
