package pob.pob_project.network;

import java.util.Random;

public class Packet {
    private String data;
    private final int sourceId;
    private final int destinationId;
    private boolean isDelayed;

    private static final Random random = new Random();

    public Packet(String data, int sourceId, int destinationId) {
        this.data = data;
        this.sourceId = sourceId;
        this.destinationId = destinationId;
    }

    public String getData() { return data; }
    public int getSourceId() { return sourceId; }
    public int getDestinationId() { return destinationId; }
    public boolean isDelayed() { return isDelayed; }
    public void setIsDelayed(boolean isDelayed) { this.isDelayed = isDelayed; }
}
