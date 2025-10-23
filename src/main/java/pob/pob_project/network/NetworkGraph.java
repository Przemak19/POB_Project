package pob.pob_project.network;

import java.util.ArrayList;
import java.util.List;

public class NetworkGraph {
    private final List<Node> nodes = new ArrayList<>();
    private final List<Connection> connections = new ArrayList<>();

    public void addNode(Node n) {
        nodes.add(n);
    }

    public void addConnection(Node a, Node b) {
        connections.add(new Connection(a, b));
        a.addNeighbor(b);
        b.addNeighbor(a);
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public List<Connection> getConnections() {
        return connections;
    }

    public List<Node> getNeighbors(Node n) {
        return n == null ? List.of() : n.getNeighbors();
    }
}
