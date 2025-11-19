package pob.pob_project.gui;

import javafx.geometry.Insets;
import javafx.scene.layout.BorderPane;
import pob.pob_project.simulation.SimulationController;

/**
 * Główne okno interfejsu graficznego aplikacji symulacyjnej.
 * Łączy trzy główne panele interfejsu:
 * <ul>
 *     <li>{@link GraphPanel} – wizualizacja sieci i transmisji danych,</li>
 *     <li>{@link ControlPanel} – narzędzia do zarządzania symulacją i węzłami,</li>
 *     <li>{@link LogPanel} – panel wyświetlający logi systemowe.</li>
 * </ul>
 */
public class MainWindow extends BorderPane {
    /** Panel odpowiedzialny za graficzną prezentację sieci komputerowej. */
    private GraphPanel graphPanel;

    /** Panel sterowania symulacją. */
    private ControlPanel controlPanel;

    /** Panel wyświetlający logi systemowe z przebiegu symulacji. */
    private LogPanel logPanel;

    /**
     * Tworzy główne okno aplikacji oraz inicjalizuje panele interfejsu.
     * @param controller Główny kontroler symulacji odpowiedzialny za logikę działania sieci.
     */
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

    /**
     * Zwraca referencję do panelu wizualizacji grafu sieci.
     * @return Obiekt {@link GraphPanel} aktualnie wyświetlany w oknie.
     */
    public GraphPanel getGraphPanel() {
        return graphPanel;
    }
}