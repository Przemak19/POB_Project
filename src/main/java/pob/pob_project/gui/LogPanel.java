package pob.pob_project.gui;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

public class LogPanel extends VBox {

    private static TextArea logArea;

    public LogPanel() {
        setStyle("-fx-background-color: #111;");
        setPrefHeight(400);

        Label title = new Label("Logi systemowe:");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefHeight(360);
        logArea.setStyle("-fx-control-inner-background: black; -fx-text-fill: #00ff00;");

        getChildren().addAll(title, logArea);
    }

    /** Metoda wywoływana przez Logger do dodawania wpisów. */
    public static void appendLog(String message) {
        Platform.runLater(() -> {
            logArea.appendText(message + "\n");
        });
    }
}
