package pob.pob_project.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import pob.pob_project.simulation.SimulationController;

/**
 * Główna klasa uruchamiająca aplikację symulacji sieci komputerowej.
 * Inicjalizuje kontroler symulacji, buduje okno główne (GUI) oraz uruchamia proces symulacji.
 * Klasa dziedziczy po {@link javafx.application.Application}, dzięki czemu może być bezpośrednio uruchamiana jako aplikacja JavaFX.
 */
public class MainApp extends Application {

    /** Główny kontroler zarządzający logiką symulacji i stanem sieci. */
    SimulationController controller;

    /**
     * Metoda startowa aplikacji JavaFX.
     * Tworzy i inicjalizuje obiekty niezbędne do działania symulacji:
     * <ul>
     *     <li>kontroler symulacji,</li>
     *     <li>strukturę sieci (węzły i połączenia),</li>
     *     <li>główne okno interfejsu graficznego,</li>
     *     <li>scenę i główne okno aplikacji.</li>
     * </ul>
     *
     * @param stage Główna scena aplikacji JavaFX.
     */
    @Override
    public void start(Stage stage) {
        controller = new SimulationController();
        controller.initializeNetwork();
        controller.startSimulation();
        MainWindow window = new MainWindow(controller);
        Scene scene = new Scene(window, 1200, 1000);

        controller.setGraphPanel(window.getGraphPanel());

        stage.setTitle("Symulacja");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Punkt wejścia programu.
     * Uruchamia aplikację JavaFX.
     * @param args Argumenty przekazywane do programu z linii komend.
     */
    public static void main(String[] args) {
        launch();
    }
}
