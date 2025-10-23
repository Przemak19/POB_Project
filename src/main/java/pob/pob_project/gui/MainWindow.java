package pob.pob_project.gui;

import javafx.geometry.Insets;
import javafx.scene.layout.BorderPane;
import pob.pob_project.simulation.SimulationController;

public class MainWindow extends BorderPane {
    private GraphPanel graphPanel;
    private ControlPanel controlPanel;
    private LogPanel logPanel;

    public MainWindow(SimulationController controller) {
        this.graphPanel = new GraphPanel(controller);
        this.controlPanel = new ControlPanel(controller, graphPanel);
        this.logPanel = new LogPanel();

        setCenter(graphPanel);
        setRight(controlPanel);
        setBottom(logPanel);

        setPadding(new Insets(10));
        setStyle("-fx-background-color: #1e1e1e;");
    }
}
