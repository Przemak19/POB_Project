package pob.pob_project.simulation;

import javafx.application.Platform;
import pob.pob_project.gui.LogPanel;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Klasa odpowiedzialna za logowanie zdarzeń w systemie symulacji.
 * Obsługuje zarówno wypisywanie logów w konsoli, jak i przekazywanie ich do panelu interfejsu graficznego {@link LogPanel}.
 * Każdy wpis logu jest automatycznie opatrzony znacznikiem czasu w formacie {@code HH:mm:ss:SSS}.
 */
public class Logger {

    /** Format daty używany w znacznikach czasu logów. */
    private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss:SSS");

    /**
     * Dodaje komunikat do logu systemowego oraz wyświetla go w konsoli i panelu GUI.
     * Metoda jest zsynchronizowana, aby zapewnić bezpieczeństwo wątkowe podczas logowania z wielu wątków symulacji.
     * @param message Treść komunikatu do zapisania w logu.
     */
    public static synchronized void log(String message) {
        String timestamp = sdf.format(new Date());
        String fullMsg = "\n[" + timestamp + "] " + message;
        System.out.println(fullMsg);

        try {
            Platform.runLater(() -> LogPanel.appendLog(fullMsg));
        } catch (Exception ignored) {}
    }
}
