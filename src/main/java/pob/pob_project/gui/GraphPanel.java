package pob.pob_project.gui;

import javafx.animation.FillTransition;
import javafx.animation.PathTransition;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Text;
import javafx.util.Duration;
import pob.pob_project.network.Node;
import pob.pob_project.simulation.SimulationController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphPanel extends Pane {

    private final int WIDTH = 700;
    private final int HEIGHT = 600;

    private SimulationController controller;
    private Map<Node, Circle> nodeCircles = new HashMap<>();
    private Map<Node, Text> nodeLabels = new HashMap<>();
    private Map<Node, Text> errorLabels = new HashMap<>();
    private List<Line> connections = new ArrayList<>();
    private Map<Node, Text> statsLabels = new HashMap<>();

    public GraphPanel(SimulationController controller) {
        this.controller = controller;
        setPrefSize(WIDTH, HEIGHT);
        drawGraph();
    }

    private void drawGraph() {
        List<Node> nodes = controller.getGraph().getNodes();
        double centerX = (double) WIDTH / 1.5;
        double centerY = (double) HEIGHT / 1.5;
        double radius = 300;

        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            double angle = 2 * Math.PI * i / nodes.size();
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);

            Circle circle = new Circle(x, y, 30, Color.LIGHTGREEN);
            circle.setStroke(Color.WHITE);
            circle.setStrokeWidth(3);

            Text label = new Text(x - 4, y + 5, String.valueOf(nodes.get(i).getId()));
            label.setFill(Color.BLACK);

            Text errorLabel = new Text(x - 25, y + 50, "");
            errorLabel.setFill(Color.RED);

            Rectangle statsBackground = new Rectangle(0, 0, 220, 130);
            statsBackground.setFill(Color.rgb(60, 60, 60, 0.8));
            statsBackground.setArcWidth(10);
            statsBackground.setArcHeight(10);
            statsBackground.setVisible(false);

            Text statsLabel = new Text(20, 20, "");
            statsLabel.setFill(Color.WHITE);
            statsLabel.setStyle("-fx-font-size: 14px;");
            statsLabel.setVisible(false);

            nodeCircles.put(node, circle);
            nodeLabels.put(node, label);
            errorLabels.put(node, errorLabel);
            statsLabels.put(node, statsLabel);

            //pokaż statystyki
            circle.setOnMouseEntered(e -> {
                updateNodeStats(node);
                statsLabel.setVisible(true);
                statsBackground.setVisible(true);
                circle.setScaleX(1.15);
                circle.setScaleY(1.15);
                circle.setStroke(Color.YELLOW);
            });

            //ukryj statystyki
            circle.setOnMouseExited(e -> {
                statsLabel.setVisible(false);
                statsBackground.setVisible(false);
                circle.setScaleX(1.0);
                circle.setScaleY(1.0);
                circle.setStroke(Color.WHITE);
            });

            getChildren().addAll(circle, label, errorLabel, statsBackground, statsLabel);
        }

        for (int i = 0; i < controller.getGraph().getConnections().size(); i++) {
            Node a = controller.getGraph().getConnections().get(i).getNodeA();
            Node b = controller.getGraph().getConnections().get(i).getNodeB();
            Circle c1 = nodeCircles.get(a);
            Circle c2 = nodeCircles.get(b);

            Line line = new Line(c1.getCenterX(), c1.getCenterY(), c2.getCenterX(), c2.getCenterY());
            line.setStroke(Color.GRAY);
            line.setStrokeWidth(2);
            connections.add(line);
            getChildren().addFirst(line);
        }
    }

    public void updateNodeStats(Node node) {
        Platform.runLater(() -> {
            Text stats = statsLabels.get(node);
            if (stats != null) {
                stats.setText(
                        String.format("Komputer: %d\n\nWysłane: %d\nOdebrane: %d\nBłędy: %d\nAktualny błąd: %s",
                                node.getId(),
                                node.getSentCount(),
                                node.getReceivedCount(),
                                node.getErrorCount(),
                                node.getCurrentErrorText()
                        )
                );
            }
            updateNodeStatus(node);
        });
    }

    /**
     * Animacja przesyłu danych między dwoma węzłami z określonym czasem.
     * Pozwala dopasować długość animacji do czasu rzeczywistego przesyłu.
     */
    public void animateTransmission(Node from, Node to, long durationMs) {
        Circle c1 = nodeCircles.get(from);
        Circle c2 = nodeCircles.get(to);
        if (c1 == null || c2 == null) return;

        // Mała kula symbolizująca pakiet
        Circle packet = new Circle(5, Color.LIMEGREEN);
        Platform.runLater(() -> getChildren().add(packet));

        Path path = new Path();
        path.getElements().add(new MoveTo(c1.getCenterX(), c1.getCenterY()));
        path.getElements().add(new LineTo(c2.getCenterX(), c2.getCenterY()));

        // Czas animacji = durationMs (ms)
        double seconds = Math.max(durationMs / 1000.0, 0.3); // minimum 0.3 sekundy dla widoczności
        PathTransition transition = new PathTransition(Duration.seconds(seconds), path, packet);
        transition.setCycleCount(1);
        transition.setOnFinished(e -> Platform.runLater(() -> getChildren().remove(packet)));
        Platform.runLater(transition::play);
    }

    public void updateNodeStatus(Node node) {
        Platform.runLater(() -> {
            Circle circle = nodeCircles.get(node);
            Text errorText = errorLabels.get(node);

            if (circle != null) {
                if (node.getCurrentFault() == null) {
                    circle.setFill(Color.LIGHTGREEN);
                } else {
                    circle.setFill(Color.TOMATO);
                }
            }

            if (errorText != null) {
                if (node.getCurrentFault() != null && node.getCurrentFault().isActive()) {
                    errorText.setText(node.getCurrentFault().getType().name());
                } else {
                    errorText.setText("");
                }
            }
        });
    }
}
