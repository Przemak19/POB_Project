package pob.pob_project.network;

import pob.pob_project.crc.CRCUtil;
import pob.pob_project.error.ErrorType;
import pob.pob_project.error.Fault;
import pob.pob_project.simulation.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Node implements Runnable {
    private final int id;
    private boolean isActive = true;
    private final BlockingQueue<Packet> incomingQueue = new LinkedBlockingQueue<>();
    private final List<Node> neighbors = new ArrayList<>();
    private Fault currentFault;
    private String polynomial = "1010";
    private final CRCUtil crcUtil;
    private Thread nodeThread;

    public Node(int id) {
        this.id = id;
        this.crcUtil = new CRCUtil();
    }

    public void addNeighbor(Node n) {
        neighbors.add(n);
    }

    public void start() {
        nodeThread = new Thread(this, "Node-" + id);
        nodeThread.start();
    }

    public boolean sendData(Node target, String message) {
        if (!isActive) {
            Logger.log("Węzeł " + id + " jest nieaktywny – nie można wysłać danych.");
            return false;
        }

        String data;
        try {
            if (currentFault != null && currentFault.getType() == ErrorType.CRC_FAILURE && currentFault.isActive()) {
                data = crcUtil.appendCRC(message, ""); //generowany jest wyjatek IndexOutOfBounds
                Logger.log("Węzeł " + id + " generuje błędne CRC. (CRC_FAILURE)");
            } else {
                data = crcUtil.appendCRC(message, polynomial);
            }
        } catch (Exception e) {
            Logger.log("Błąd przy obliczaniu CRC w węźle " + id);
            System.out.println(e.getMessage());
            return false;
        }

        Packet packet = new Packet(data, this.id, target.getId());
        Logger.log("Węzeł " + id + " wysyła pakiet z wiadomością: " + message +
                " do węzła " + target.getId() + ": pakiet bitów zabezpieczonych CRC: " + data);

        // Możliwe błędy transmisji: DELAY, PACKET_DROP
        if (currentFault != null && currentFault.isActive()) {
            if (currentFault.getType() == ErrorType.PACKET_DROP) {
                Logger.log("Węzeł " + id + ": pakiet został utracony. (PACKET_DROP)");
                return false;
            }
            if (currentFault.getType() == ErrorType.DELAY) {
                    packet.setIsDelayed(true);
                    Logger.log("Opóźnienie pakietu wysłanego z węzła " + id + ". (DELAY)");
            }
        }

        try {
            target.getQueue().put(packet);
        } catch (InterruptedException e) {
            Logger.log("Błąd wysyłki pakietu od węzła " + this.id);
            return false;
        }
        return true;
    }

    public void setCrcPolynomial(String poly) {
        this.polynomial = poly;
    }

    public void receivePacket(Packet packet) throws InterruptedException {
        if (!isActive) {
            Logger.log("Węzeł " + id + " nieaktywny – pakiet odrzucony.");
            return;
        }

        if(packet.isDelayed()) {
            int delay = new Random().nextInt(800);
            Logger.log("Opóźnienie jest równe " + delay + " ms.");
            Thread.sleep(delay);
        }

        Thread.sleep(200);

        boolean valid = crcUtil.validateCRC(packet, polynomial);

        if (valid) {
            Logger.log("Węzeł " + id + " odebrał poprawny pakiet od węzła " + packet.getSourceId() +
                    " z wiadomością: " + crcUtil.extractMessage(packet.getData(), polynomial) + ", bity: " + packet.getData());
        } else {
            Logger.log("Węzeł " + id + " wykrył BŁĄD w pakiecie od węzła " + packet.getSourceId() +
                    " (CRC niepoprawne), wiadomość: " + crcUtil.extractMessage(packet.getData(), polynomial) + ", bity: " + packet.getData());
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                Packet packet = incomingQueue.take();
                receivePacket(packet);
            } catch (InterruptedException e) {
                Logger.log("Węzeł " + id + " został zatrzymany.");
                break;
            }
        }
    }

    public void injectFault(ErrorType type) {
        this.currentFault = new Fault(type);
        this.isActive = type != ErrorType.NODE_FREEZE;
        Logger.log("Węzeł " + id + ": wstrzyknięto błąd " + type);
    }

    public void repairFault() {
        if (currentFault != null) {
            Logger.log("Węzeł " + id + ": usterka " + currentFault.getType() + " usunięta.");
            currentFault.deactivate();
            isActive = true;
            currentFault = null;
        }
    }

    public int getId() { return id; }
    public BlockingQueue<Packet> getQueue() { return incomingQueue; }
    public boolean isActive() { return isActive; }
    public List<Node> getNeighbors() { return neighbors; }
}
