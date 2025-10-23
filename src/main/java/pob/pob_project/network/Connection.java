package pob.pob_project.network;

public class Connection {
    private final Node nodeA;
    private final Node nodeB;

    public Connection(Node a, Node b) {
        this.nodeA = a;
        this.nodeB = b;
    }

    public Node getNodeA() {
        return nodeA;
    }
    public Node getNodeB() {
        return nodeB;
    }
}
