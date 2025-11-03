package pob.pob_project.gui;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;


public class LogPanel extends VBox {

    private static TextFlow logFlow;

    public LogPanel() {
        setStyle("-fx-background-color: #111;");
        setPrefHeight(400);

        Label title = new Label("Logi systemowe:");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        logFlow = new TextFlow();
        logFlow.setPrefHeight(360);
        logFlow.setLineSpacing(4);
        logFlow.setStyle("-fx-padding: 10;");
        logFlow.setStyle("-fx-font-size: 14px;");

        ScrollPane scrollPane = new ScrollPane(logFlow);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(360);
        scrollPane.setStyle("-fx-background: black; -fx-border-color: gray;");

        getChildren().addAll(title, scrollPane);
    }

    /** Dodaje log z kolorowaniem fragmentów. */
    public static void appendLog(String message) {
        Platform.runLater(() -> {

            Text timestamp = new Text(extractTime(message));
            timestamp.setFill(Color.LIMEGREEN);

            Text nodeId1 = new Text(extractNodeId1(message));
            nodeId1.setFill(Color.TOMATO);
            nodeId1.setStyle("-fx-font-weight: bolder;");

            Text nodeId2 = new Text(extractNodeId2(message));
            nodeId2.setFill(Color.TOMATO);
            nodeId2.setStyle("-fx-font-weight: bolder;");

            Text bits = new Text(extractBits(message));
            bits.setFill(Color.YELLOWGREEN);
            bits.setStyle("-fx-font-weight: bold;");

            Text rest1 = new Text(extractRest1(message));
            rest1.setFill(Color.WHEAT);

            Text rest2 = new Text(extractRest2(message));
            rest2.setFill(Color.WHEAT);

            if(nodeId1.getText().isEmpty() && nodeId2.getText().isEmpty()) {
                logFlow.getChildren().addAll(timestamp, rest1, rest2);
            } else if(nodeId1.getText().isEmpty() && !nodeId2.getText().isEmpty()) {
                logFlow.getChildren().addAll(timestamp, rest1, nodeId2, rest2, bits);
            } else if(nodeId2.getText().isEmpty() && !nodeId1.getText().isEmpty()) {
                logFlow.getChildren().addAll(timestamp, nodeId1, rest1, rest2, bits);
            } else {
                logFlow.getChildren().addAll(timestamp, nodeId1, rest1, nodeId2, rest2, bits);
            }
        });
    }

    private static String extractTime(String msg) {
        int end = msg.indexOf("]");
        return (end > 0) ? msg.substring(0, end + 1) + " " : "";
    }

    private static String extractNodeId1(String msg) {
        if (msg.contains("Komputer ")) {
            int start = msg.indexOf("Komputer ");
            int end = msg.indexOf("", start + 9);
            return msg.substring(start, end + 1);
        }
        return "";
    }

    private static String extractRest1(String msg) {
        if (msg.contains("komputer ")) {
            int start = msg.indexOf("Komputer ");
            int end = msg.indexOf("komputer");
            return msg.substring(start + 10, end);
        }
        return "";
    }

    private static String extractNodeId2(String msg) {
        if (msg.contains("komputer ")) {
            int start = msg.indexOf("komputer ");
            int end = msg.indexOf("", start + 9);
            return msg.substring(start, end + 1);
        }
        return "";
    }

    private static String extractBits(String msg) {
        if (msg.contains("]: ")) {
            int start = msg.indexOf("]: ") + 3;
            int end = msg.indexOf(' ', start);

            if (end == -1) end = msg.length();
            if (start < end) {
                return msg.substring(start, end);
            }
        }
        return "";
    }

    private static String extractRest2(String msg) {
        String cleaned = msg;
        cleaned = cleaned.replace(extractTime(msg), "");
        cleaned = cleaned.replace(extractRest1(msg), "");
        cleaned = cleaned.replace(extractNodeId1(msg), "");
        cleaned = cleaned.replace(extractNodeId2(msg), "");
        cleaned = cleaned.replace(extractBits(msg), "");
        return cleaned;
    }

    public static void clearLog() {
        Platform.runLater(() -> logFlow.getChildren().clear());
    }
}
