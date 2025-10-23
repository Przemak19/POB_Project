package pob.pob_project.simulation;

import pob.pob_project.error.ErrorInjector;
import pob.pob_project.error.ErrorType;
import pob.pob_project.network.NetworkGraph;
import pob.pob_project.network.Node;

public class SimulationController {
    private final NetworkGraph graph;
    private final ErrorInjector errorInjector;
    private String crcPolynomial = "1010";

    public SimulationController() {
        graph = new NetworkGraph();
        errorInjector = new ErrorInjector();
    }

    public void initializeNetwork() {
        // Tworzenie 10 węzłów
        for (int i = 0; i < 10; i++) {
            Node node = new Node(i);
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

    public void startSimulation() {
        for (Node node : graph.getNodes()) {
            node.start();
        }
        Logger.log("Symulacja uruchomiona.");
    }

    //metoda do sprawdzania czy wielomian ma przynajmniej 2 bity, czy składa się z zer i jedynek oraz czy zaczyna się od 1
    public boolean checkCrcPolynomial(String polynomial) {
        if (polynomial == null) return false;

        return polynomial.length() >= 2
                && polynomial.matches("[01]+")
                && polynomial.charAt(0) == '1';
    }

    public boolean checkNeighbors(Node src, Node dst) {
        for (Node node : graph.getNeighbors(src)) {
            if(node.equals(dst)) {
                return true;
            }
        }
        return false;
    }

    public void injectError(Node node, ErrorType type) {
        errorInjector.applyError(node, type);
    }

    public void repairNode(Node node) {
        node.repairFault();
    }

    public NetworkGraph getGraph() {
        return graph;
    }

    public String getCrcPolynomial() {
        return crcPolynomial;
    }

    public void setCrcPolynomial(String newPoly) {
        this.crcPolynomial = newPoly;
        for (Node node : graph.getNodes()) {
            node.setCrcPolynomial(newPoly);
        }
        Logger.log("Zmieniono wielomian CRC na: " + newPoly);
    }
}
