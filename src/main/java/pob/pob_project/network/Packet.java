package pob.pob_project.network;

import java.util.Random;

public class Packet {
    private String data;
    private final int sourceId;
    private final int destinationId;
    private boolean isDelayed;
    private int delay;

    private boolean isAck = false;        // czy to pakiet potwierdzenia (ACK)
    private boolean ackPositive = false;  // true – ACK pozytywny, false – żądanie retransmisji

    private static final Random random = new Random();

    public Packet(String data, int sourceId, int destinationId) {
        this.data = data;
        this.sourceId = sourceId;
        this.destinationId = destinationId;
        this.delay = 200;
    }

    public static Packet createAckPacket(int sourceId, int destinationId, boolean ackPositive) {
        Packet ack = new Packet("ACK", sourceId, destinationId);
        ack.isAck = true;
        ack.ackPositive = ackPositive;
        return ack;
    }

    public String getData() { return data; }
    public int getSourceId() { return sourceId; }
    public int getDestinationId() { return destinationId; }
    public boolean isDelayed() { return isDelayed; }
    public void setIsDelayed(boolean isDelayed) { this.isDelayed = isDelayed; }
    public boolean isAck() { return isAck; }
    public boolean isAckPositive() { return ackPositive; }
    public void setAck(boolean ack) { isAck = ack; }
    public void setAckPositive(boolean ackPositive) { this.ackPositive = ackPositive; }
    public void setDelay(int delay) { this.delay = delay; }
    public int getDelay() { return delay; }
}
