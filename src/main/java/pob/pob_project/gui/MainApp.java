package pob.pob_project.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pob.pob_project.simulation.SimulationController;

public class MainApp extends Application {

    SimulationController controller;

    @Override
    public void start(Stage stage) {
        controller = new SimulationController();
        controller.initializeNetwork();
        controller.startSimulation();

        MainWindow window = new MainWindow(controller);
        Scene scene = new Scene(window, 1000, 1000);

        stage.setTitle("Symulacja");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
