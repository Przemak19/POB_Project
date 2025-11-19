package pob.pob_project.network;

import java.util.ArrayList;
import java.util.List;

/**
 * Reprezentuje strukturę grafu sieciowego używaną w symulacji.
 * Graf składa się z węzłów ({@link Node}) połączonych ze sobą ({@link Connection}).
 * Klasa umożliwia dodawanie nowych węzłów oraz tworzenie połączeń między nimi, które są dwukierunkowe.
 */
public class NetworkGraph {
    /** Lista wszystkich węzłów w grafie. */
    private final List<Node> nodes = new ArrayList<>();

    /** Lista wszystkich połączeń (krawędzi) pomiędzy węzłami. */
    private final List<Connection> connections = new ArrayList<>();

    /**
     * Dodaje nowy węzeł do grafu sieciowego.
     * @param n Węzeł, który ma zostać dodany do grafu.
     */
    public void addNode(Node n) {
        nodes.add(n);
    }

    /**
     * Tworzy dwukierunkowe połączenie pomiędzy dwoma węzłami.
     * Połączenie jest rejestrowane w grafie oraz aktualizuje listy sąsiadów w obu węzłach.
     * @param a Pierwszy węzeł połączenia.
     * @param b Drugi węzeł połączenia.
     */
    public void addConnection(Node a, Node b) {
        connections.add(new Connection(a, b));
        a.addNeighbor(b);
        b.addNeighbor(a);
    }

    /**
     * Zwraca listę wszystkich połączeń w grafie.
     * @return Lista obiektów {@link Connection}.
     */
    public List<Connection> getConnections() {
        return connections;
    }

    /**
     * Zwraca listę wszystkich węzłów w grafie.
     * @return Lista obiektów {@link Node}.
     */
    public List<Node> getNodes() {
        return nodes;
    }

    /**
     * Zwraca listę sąsiadów dla danego węzła.
     * Jeśli przekazany węzeł jest wartością {@code null}, zwracana jest pusta lista.
     * @param n Węzeł, którego sąsiedzi mają zostać zwróceni.
     * @return Lista sąsiadów lub pusta lista, jeśli węzeł nie istnieje.
     */
    public List<Node> getNeighbors(Node n) {
        return n == null ? List.of() : n.getNeighbors();
    }
}
