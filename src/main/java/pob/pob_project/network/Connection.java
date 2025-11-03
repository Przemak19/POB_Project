package pob.pob_project.network;

/**
 * Reprezentuje połączenie pomiędzy dwoma węzłami ({@link Node}) w grafie sieci.
 * Obiekt tej klasy odpowiada połączeniu w topologii sieci, pozwalającej na przesyłanie pakietów pomiędzy dwoma węzłami.
 */
public class Connection {
    /** Pierwszy węzeł połączenia. */
    private final Node nodeA;

    /** Drugi węzeł połączenia. */
    private final Node nodeB;

    /**
     * Tworzy połączenie pomiędzy dwoma węzłami sieci.
     * @param a Pierwszy węzeł połączenia.
     * @param b Drugi węzeł połączenia.
     */
    public Connection(Node a, Node b) {
        this.nodeA = a;
        this.nodeB = b;
    }

    /**
     * Zwraca pierwszy węzeł połączenia.
     * @return Obiekt {@link Node} będący pierwszym końcem połączenia.
     */
    public Node getNodeA() {
        return nodeA;
    }

    /**
     * Zwraca drugi węzeł połączenia.
     * @return Obiekt {@link Node} będący drugim końcem połączenia.
     */
    public Node getNodeB() {
        return nodeB;
    }
}
