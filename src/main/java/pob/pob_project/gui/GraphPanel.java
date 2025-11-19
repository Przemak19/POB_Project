package pob.pob_project.gui;

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

/**
 * Klasa odpowiedzialna za wizualizację grafu sieci w symulacji.
 * Reprezentuje węzły, połączenia między nimi oraz wizualne efekty transmisji danych i błędów.
 */
public class GraphPanel extends Pane {

    /** Wysokość panelu graficznego w pikselach. */
    private final int HEIGHT = 400;

    /** Szerokość panelu graficznego w pikselach. */
    private final int WIDTH = 700;

    /** Kontroler zarządzający logiką i stanem symulacji. */
    private SimulationController controller;

    /** Mapa powiązań węzłów z ich graficznymi reprezentacjami. */
    private Map<Node, Circle> nodeCircles = new HashMap<>();

    /** Mapa etykiet z identyfikatorami węzłów. */
    private Map<Node, Text> nodeLabels = new HashMap<>();

    /** Mapa etykiet informujących o błędach węzłów. */
    private Map<Node, Text> errorLabels = new HashMap<>();

    /** Lista linii reprezentujących połączenia pomiędzy węzłami. */
    private List<Line> connections = new ArrayList<>();

    /** Mapa etykiet wyświetlających statystyki węzłów. */
    private Map<Node, Text> statsLabels = new HashMap<>();

    /**
     * Konstruktor klasy GraphPanel.
     * Inicjalizuje panel i rysuje graf w oparciu o dane z kontrolera symulacji.
     * @param controller Kontroler symulacji zarządzający węzłami i połączeniami.
     */
    public GraphPanel(SimulationController controller) {
        this.controller = controller;
        setPrefSize(WIDTH, HEIGHT);
        drawGraph();
    }

    /**
     * Tworzy i rozmieszcza graficzne elementy reprezentujące węzły i połączenia grafu.
     * Każdy węzeł ma etykietę, kolor, statystyki oraz reakcję na najechanie kursorem.
     */
    private void drawGraph() {
        List<Node> nodes = controller.getGraph().getNodes();
        double centerX = (double) WIDTH / 1.5;
        double centerY = (double) HEIGHT / 1.5;
        double radius = 220;

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

    /**
     * Aktualizuje etykietę statystyk dla danego węzła (ilość wysłanych, odebranych i błędnych pakietów).
     * @param node Węzeł, którego statystyki mają zostać zaktualizowane.
     */
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
     * Animuje wizualny przesył danych pomiędzy dwoma węzłami.
     * Na ekranie pojawia się mała kula symbolizująca pakiet, poruszająca się wzdłuż połączenia między węzłami.
     * @param from Węzeł źródłowy (nadawca).
     * @param to Węzeł docelowy (odbiorca).
     * @param durationMs Czas trwania animacji w milisekundach.
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

    /**
     * Aktualizuje wygląd węzła (kolor i etykietę błędu) w zależności od jego aktualnego stanu.
     * Zielony — węzeł działa poprawnie, czerwony — wystąpił błąd.
     * @param node Węzeł, którego stan ma zostać zaktualizowany.
     */
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
