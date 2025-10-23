package pob.pob_project.simulation;

import javafx.application.Platform;
import pob.pob_project.gui.LogPanel;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss:SSS");

    public static synchronized void log(String message) {
        String timestamp = sdf.format(new Date());
        String fullMsg = "\n[" + timestamp + "] " + message;
        System.out.println(fullMsg);

        // Jeśli GUI działa — wyślij do logArea
        try {
            Platform.runLater(() -> LogPanel.appendLog(fullMsg));
        } catch (Exception ignored) {}
    }
}
