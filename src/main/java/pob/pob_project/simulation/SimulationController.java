package pob.pob_project.simulation;

import pob.pob_project.error.ErrorInjector;
import pob.pob_project.error.ErrorType;
import pob.pob_project.gui.GraphPanel;
import pob.pob_project.network.NetworkGraph;
import pob.pob_project.network.Node;

/**
 * Klasa nadrzędna sterująca przebiegiem symulacji sieci.
 * Odpowiada za inicjalizację topologii sieci (tworzenie węzłów i połączeń), uruchamianie procesów symulacyjnych, konfigurację wielomianu CRC oraz obsługę błędów wstrzykiwanych do węzłów.
 */
public class SimulationController {
    /** Reprezentacja grafu sieciowego składającego się z węzłów i połączeń. */
    private final NetworkGraph graph;

    /** Moduł odpowiedzialny za wstrzykiwanie błędów do węzłów. */
    private final ErrorInjector errorInjector;

    /** Aktualnie ustawiony wielomian CRC w formacie binarnym (np. "1010"). */
    private String crcPolynomial;

    /** Odwołanie do panelu graficznego, wizualizującego sieć i statystyki. */
    private GraphPanel graphPanel;


    /**
     * Tworzy nową instancję kontrolera symulacji.
     */
    public SimulationController() {
        graph = new NetworkGraph();
        errorInjector = new ErrorInjector();
    }

    /**
     * Inicjalizuje sieć poprzez utworzenie 10 węzłów i zdefiniowanie podstawowych połączeń między nimi.
     */
    public void initializeNetwork() {
        // Tworzenie 10 węzłów
        for (int i = 0; i < 10; i++) {
            Node node = new Node(i, this);
            graph.addNode(node);
        }

        // Tworzenie prostych połączeń (graf liniowy)
        for (int i = 0; i < 5; i++) {
            graph.addConnection(graph.getNodes().get(i), graph.getNodes().get(i + 4));
        }
        graph.addConnection(graph.getNodes().get(0), graph.getNodes().get(2));
        graph.addConnection(graph.getNodes().get(0), graph.getNodes().get(5));
        graph.addConnection(graph.getNodes().get(1), graph.getNodes().get(7));
        graph.addConnection(graph.getNodes().get(9), graph.getNodes().get(0));
        graph.addConnection(graph.getNodes().get(8), graph.getNodes().get(3));
        graph.addConnection(graph.getNodes().get(4), graph.getNodes().get(9));
        graph.addConnection(graph.getNodes().get(6), graph.getNodes().get(7));

        Logger.log("Zainicjalizowano sieć z 10 węzłami i połączeniami.");
    }

    /**
     * Weryfikuje poprawność formatu wielomianu CRC.
     * @param polynomial Ciąg bitów reprezentujący wielomian CRC.
     * @return Wartość {@code true} Jeśli wielomian jest poprawny (ma długość 2–32 bity, zawiera tylko zera i jedynki oraz zaczyna się od '1').
     */
    public boolean checkCrcPolynomial(String polynomial) {
        if (polynomial == null) return false;

        return polynomial.length() >= 2
                && polynomial.length() <= 32
                && polynomial.matches("[01]+")
                && polynomial.charAt(0) == '1';
    }

    /**
     * Uruchamia symulację, aktywując wszystkie węzły w sieci oraz ustawiając domyślny wielomian CRC.
     */
    public void startSimulation() {
        for (Node node : graph.getNodes()) {
            node.start();
        }
        setCrcPolynomial("1010");
        Logger.log("Symulacja uruchomiona.");
    }

    /**
     * Sprawdza, czy dwa węzły są bezpośrednimi sąsiadami w grafie.
     * @param src Węzeł źródłowy.
     * @param dst Węzeł docelowy.
     * @return Wartość {@code true} jeśli węzły są połączone, w przeciwnym razie {@code false}.
     */
    public boolean checkNeighbors(Node src, Node dst) {
        for (Node node : graph.getNeighbors(src)) {
            if(node.equals(dst)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Wstrzykuje określony typ błędu do wybranego węzła sieci.
     * @param node Węzeł, do którego zostanie wprowadzony błąd.
     * @param type Typ błędu ({@link ErrorType}).
     */
    public void injectError(Node node, ErrorType type) {
        errorInjector.applyError(node, type);
    }

    /**
     * Naprawia awarię w wybranym węźle (usuwa aktualny błąd).
     * @param node Węzeł, w którym zostanie usunięta usterka.
     */
    public void repairNode(Node node) {
        node.repairFault();
    }


    /** @return Graf symulacji. */
    public NetworkGraph getGraph() {
        return graph;
    }

    /** @return Aktualnie używany wielomian CRC. */
    public String getCrcPolynomial() {
        return crcPolynomial;
    }


    /**
     * Ustawia nowy wielomian CRC i propaguje go do wszystkich węzłów sieci.
     * @param newPoly Nowy wielomian CRC (ciąg bitów).
     */
    public void setCrcPolynomial(String newPoly) {
        this.crcPolynomial = newPoly;
        for (Node node : graph.getNodes()) {
            node.setCrcPolynomial(newPoly);
        }
        Logger.log("Zmieniono wielomian CRC na: " + newPoly);
    }

    /** Ustawia panel graficzny odpowiedzialny za wizualizację sieci. */
    public void setGraphPanel(GraphPanel panel) {
        this.graphPanel = panel;
    }

    /** @return Panel graficzny aktualnie używany przez kontroler. */
    public GraphPanel getGraphPanel() {
        return graphPanel;
    }
}
