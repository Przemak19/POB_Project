package pob.pob_project.gui;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import pob.pob_project.error.ErrorType;
import pob.pob_project.network.Node;
import pob.pob_project.simulation.SimulationController;

public class ControlPanel extends VBox {

    private SimulationController controller;
    private GraphPanel graphPanel;

    public ControlPanel(SimulationController controller, GraphPanel graphPanel) {
        this.controller = controller;
        this.graphPanel = graphPanel;

        setSpacing(15);
        setPadding(new Insets(15));
        setStyle("-fx-background-color: #2b2b2b;");

        Label title = new Label("Sterowanie symulacją");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");

        // --- Sekcja: wybór węzła
        Label nodeLabel = new Label("ID węzła:");
        nodeLabel.setStyle("-fx-text-fill: white;");
        Spinner<Integer> nodeSpinner = new Spinner<>(0, 9, 0);

        // --- Sekcja: wybór błędu
        Label errorLabel = new Label("Rodzaj błędu:");
        errorLabel.setStyle("-fx-text-fill: white;");
        ComboBox<ErrorType> errorTypeBox = new ComboBox<>();
        errorTypeBox.getItems().addAll(ErrorType.values());
        errorTypeBox.getSelectionModel().selectFirst();

        Button injectBtn = new Button("Wstrzyknij błąd");
        Button repairBtn = new Button("Napraw węzeł");

        injectBtn.setOnAction(e -> {
            Node node = controller.getGraph().getNodes().get(nodeSpinner.getValue());
            ErrorType type = errorTypeBox.getValue();
            controller.injectError(node, type);
            graphPanel.updateNodeColor(node, false);
            graphPanel.flashNode(node);
        });

        repairBtn.setOnAction(e -> {
            Node node = controller.getGraph().getNodes().get(nodeSpinner.getValue());
            controller.repairNode(node);
            graphPanel.updateNodeColor(node, true);
        });

        // --- Sekcja: wysyłanie danych
        Label sendLabel = new Label("Wysyłanie danych:");
        sendLabel.setStyle("-fx-text-fill: white;");
        TextField dataField = new TextField("cześć");
        Label fromLabel = new Label("Nadawca:");
        fromLabel.setStyle("-fx-text-fill: white;");
        Spinner<Integer> srcSpinner = new Spinner<>(0, 9, 0);
        Label toLabel = new Label("Adresat:");
        toLabel.setStyle("-fx-text-fill: white;");
        Label messageLabel = new Label("Wiadomość:");
        messageLabel.setStyle("-fx-text-fill: white;");
        Spinner<Integer> dstSpinner = new Spinner<>(0, 9, 1);
        Button sendBtn = new Button("Wyślij");

        sendBtn.setOnAction(e -> {
            Node src = controller.getGraph().getNodes().get(srcSpinner.getValue());
            Node dst = controller.getGraph().getNodes().get(dstSpinner.getValue());

            if(src == dst) {
                showAlert("Błędnie wybrane węzły", "Węzeł nie może wysłać wiadomości do samego siebie.");
            }
            else if(!controller.checkNeighbors(src, dst)) {
                showAlert("Błędnie wybrane węzły", "Wybrane węzły nie są ze sobą połączone.");
            } else {
                boolean success = src.sendData(dst, dataField.getText());
                graphPanel.animateTransmission(src, dst, success);
            }

        });

        // --- Sekcja: CRC
        Label crcLabel = new Label("Wielomian do CRC:");
        crcLabel.setStyle("-fx-text-fill: white;");
        TextField crcField = new TextField(controller.getCrcPolynomial());
        Button crcApply = new Button("Zmień wielomian");

        crcApply.setOnAction(e -> {
            if(controller.checkCrcPolynomial(crcField.getText().trim())) {
                controller.setCrcPolynomial(crcField.getText().trim());
            } else {
                showAlert("Nieprawidłowy wielomian CRC", "Wielomian musi mieć przynajmniej 2 bity, składać się z 0 i 1 oraz zaczynać się od 1.");
            }

        });

        getChildren().addAll(
                title, new Separator(),
                nodeLabel, nodeSpinner,
                errorLabel, errorTypeBox, injectBtn, repairBtn,
                new Separator(),
                sendLabel,
                fromLabel, srcSpinner,
                toLabel, dstSpinner,
                messageLabel, dataField, sendBtn,
                new Separator(),
                crcLabel, crcField, crcApply
        );
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
