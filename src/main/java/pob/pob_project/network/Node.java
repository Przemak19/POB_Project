package pob.pob_project.network;

import pob.pob_project.crc.CRCUtil;
import pob.pob_project.error.ErrorType;
import pob.pob_project.error.Fault;
import pob.pob_project.simulation.Logger;
import pob.pob_project.simulation.SimulationController;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Node implements Runnable {

    private final int id;
    private boolean isActive = true;
    private final BlockingQueue<Packet> incomingQueue = new LinkedBlockingQueue<>();
    private final List<Node> neighbors = new ArrayList<>();
    private Fault currentFault;
    private String polynomial;
    private final CRCUtil crcUtil;
    private Thread nodeThread;
    private final SimulationController controller;
    private final Map<Integer, Packet> lastSentPackets = new HashMap<>();

    private int sentCount = 0;
    private int receivedCount = 0;
    private int errorCount = 0;

    public Node(int id, SimulationController controller) {
        this.id = id;
        this.controller = controller;
        this.crcUtil = new CRCUtil();
    }

    public void addNeighbor(Node n) {
        neighbors.add(n);
    }

    public void start() {
        nodeThread = new Thread(this, "Komputer-" + id);
        nodeThread.start();
    }

    /**
     * Wysyła pakiet danych do innego węzła.
     */
    public void sendData(Node target, String message) {
        if (!isActive) {
            Logger.log("Komputer " + id + ": nieaktywny - nie można wysłać danych. (NODE_FREEZE)");
            errorCount++;
            updateStatsInUI();
            return;
        }

        String data;
        try {
            if (currentFault != null && currentFault.getType() == ErrorType.CRC_FAILURE && currentFault.isActive()) {
                data = crcUtil.appendCRC(message, ""); // generuje wyjątek
                Logger.log("Komputer " + id + ": generuje błędne CRC. (CRC_FAILURE)");
            } else {
                data = crcUtil.appendCRC(message, polynomial);
            }
        } catch (Exception e) {
            Logger.log("Komputer " + id + ": błąd przy obliczaniu CRC.");
            errorCount++;
            updateStatsInUI();
            return;
        }

        String correctData = data;

        if (currentFault != null && currentFault.isActive() && currentFault.getType() == ErrorType.BIT_FLIP) {
            data = flipRandomBit(data, polynomial);
            errorCount++;
            updateStatsInUI();
        }

        Packet packet = new Packet(data, this.id, target.getId());
        Packet correctPacket = new Packet(correctData, this.id, target.getId());

        if (currentFault != null && currentFault.getType() == ErrorType.DELAY) {
            int currentDelay = new Random().nextInt(700);
            packet.setIsDelayed(true);
            packet.setDelay(currentDelay + packet.getDelay());
            errorCount++;
            updateStatsInUI();
            Logger.log("Komputer " + id + ": pakiet opóźniony o " + packet.getDelay() + " ms.");
        }

        // Zapisz ostatnio wysłany pakiet (do ewentualnej retransmisji)
        lastSentPackets.put(target.getId(), correctPacket);
        sentCount++;
        updateStatsInUI();

        Logger.log("Komputer " + id + ": wysyła pakiet do komputer " + target.getId() +
                " z wiadomością: " + message + ", CRC [bity]: " + crcUtil.extractCRC(correctData, polynomial) + " ");

        // Utrata pakietu
        if (currentFault != null && currentFault.getType() == ErrorType.PACKET_DROP) {
            Logger.log("Komputer " + id + ": pakiet został utracony. (PACKET_DROP)");
            errorCount++;
            updateStatsInUI();
            return;
        }

        try {
            target.getQueue().put(packet);
        } catch (InterruptedException e) {
            Logger.log("Komputer " + this.id + ": błąd wysyłki pakietu do komputer " + target.getId() + ".");
            errorCount++;
            updateStatsInUI();
        }
    }

    /**
     * Odbieranie pakietu.
     */
    public void receivePacket(Packet packet) throws InterruptedException {
        if (!isActive) {
            Logger.log("Komputer " + id + ": nieaktywny - pakiet odrzucony.");
            errorCount++;
            updateStatsInUI();
            return;
        }

        Node sourceNode = findNeighborById(packet.getSourceId());
        if (sourceNode == null) return;

        // Animacja przychodzącego pakietu
        if (controller != null && controller.getGraphPanel() != null) {
            controller.getGraphPanel().animateTransmission(sourceNode, this, packet.getDelay());
        }

        if(packet.isDelayed()) {
            errorCount++;
        }

        Thread.sleep(packet.getDelay());

        // Obsługa ACK
        if (packet.isAck()) {
            if (packet.isAckPositive()) {
                Logger.log("Komputer " + id + ": otrzymał potwierdzenie ACK od komputer " + packet.getSourceId() + ".");
                receivedCount++;
            } else {
                Logger.log("Komputer " + id + ": otrzymał NACK - retransmisja pakietu.");
                Packet last = lastSentPackets.get(packet.getSourceId());
                receivedCount++;
                if (last != null) {
                    repairFault();
                    sendData(sourceNode, crcUtil.extractMessage(last.getData(), polynomial));
                }
            }
            return;
        }

        // Walidacja danych
        boolean valid = crcUtil.validateCRC(packet, polynomial);

        if (valid) {
            Logger.log("Komputer " + id + ": odebrał POPRAWNY pakiet od komputer " + packet.getSourceId() +
                    " " + crcUtil.extractMessage(packet.getData(), polynomial) + ", CRC [bity]: " + crcUtil.extractCRC(packet.getData(), polynomial) + " ");
            receivedCount++;
            // Wyślij ACK pozytywny
            Packet ack = Packet.createAckPacket(id, packet.getSourceId(), true);
            sourceNode.getQueue().put(ack);
            sentCount++;
        } else {
            Logger.log("Komputer " + id + ": wykrył BŁĄD w pakiecie od komputer " + packet.getSourceId() +
                    ": " + crcUtil.extractMessage(packet.getData(),polynomial) + " - CRC niepoprawne, CRC [bity]: " + crcUtil.extractCRC(packet.getData(), polynomial) + " ");
            receivedCount++;
            errorCount++;
            // Wyślij ACK negatywny (żądanie retransmisji)
            Packet nack = Packet.createAckPacket(id, packet.getSourceId(), false);
            sourceNode.getQueue().put(nack);
            sentCount++;
        }
        updateStatsInUI();
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

    private Node findNeighborById(int id) {
        for (Node n : neighbors) {
            if (n.getId() == id) return n;
        }
        return null;
    }

    private String flipRandomBit(String data, String polynomial) {
        if (data == null || data.isEmpty()) return data;
        Random random = new Random();
        int index = random.nextInt(data.length()-polynomial.length() - 1, data.length());
        char[] bits = data.toCharArray();
        bits[index] = (bits[index] == '0') ? '1' : '0';
        return new String(bits);
    }

    public void injectFault(ErrorType type) {
        this.currentFault = new Fault(type);
        this.isActive = type != ErrorType.NODE_FREEZE;
        Logger.log("Komputer " + id + ": wstrzyknięto błąd " + type);
    }

    public void repairFault() {
        if (currentFault != null) {
            Logger.log("Komputer " + id + ": usterka " + currentFault.getType() + " usunięta.");
            currentFault.deactivate();
            isActive = true;
            currentFault = null;
            controller.getGraphPanel().updateNodeStatus(this);
        }
    }

    public void setCrcPolynomial(String polynomial) {
        this.polynomial = polynomial;
    }

    private void updateStatsInUI() {
        if (controller != null && controller.getGraphPanel() != null)
            controller.getGraphPanel().updateNodeStats(this);
    }

    // --- Gettery ---
    public int getId() { return id; }
    public BlockingQueue<Packet> getQueue() { return incomingQueue; }
    public boolean isActive() { return isActive; }
    public List<Node> getNeighbors() { return neighbors; }
    public Fault getCurrentFault() { return currentFault; }
    public String getCurrentErrorText() {
        return currentFault != null && currentFault.isActive() ? currentFault.getType().name() : "brak";
    }
    public int getErrorCount() { return errorCount; }
    public int getSentCount() { return sentCount; }
    public int getReceivedCount() { return receivedCount; }
}
