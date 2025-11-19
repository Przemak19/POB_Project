package pob.pob_project.network;

import pob.pob_project.crc.CRCUtil;
import pob.pob_project.error.ErrorType;
import pob.pob_project.error.Fault;
import pob.pob_project.simulation.Logger;
import pob.pob_project.simulation.SimulationController;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * Reprezentuje pojedynczy komputer (węzeł) w symulowanej sieci.
 * Każdy węzeł działa w osobnym wątku, obsługując kolejkę przychodzących pakietów.
 * Może wysyłać dane, odbierać pakiety, wykrywać błędy CRC oraz symulować różne typy usterek.
 */
public class Node implements Runnable {

    /** Identyfikator węzła w sieci. */
    private final int id;

    /** Czy węzeł jest aktywny (może wysyłać i odbierać dane). */
    private boolean isActive = true;

    /** Kolejka pakietów oczekujących na przetworzenie. */
    private final BlockingQueue<Packet> incomingQueue = new LinkedBlockingQueue<>();

    /** Lista sąsiadów (węzłów bezpośrednio połączonych z tym). */
    private final List<Node> neighbors = new ArrayList<>();

    /** Wielomian używany do obliczania CRC. */
    private String polynomial;

    /** Aktualna usterka (jeśli występuje). */
    private Fault currentFault;

    /** Narzędzie do generowania i weryfikacji CRC. */
    private final CRCUtil crcUtil;

    /** Wątek obsługujący działanie węzła. */
    private Thread nodeThread;

    /** Referencja do kontrolera symulacji. */
    private final SimulationController controller;

    /** Ostatnio wysłane pakiety, używane do retransmisji po błędzie. */
    private final Map<Integer, Packet> lastSentPackets = new HashMap<>();

    /** Liczba wysłanych pakietów. */
    private int sentCount = 0;

    /** Liczba odebranych pakietów. */
    private int receivedCount = 0;

    /** Liczba błędów wykrytych przez ten węzeł. */
    private int errorCount = 0;


    /**
     * Tworzy nowy węzeł o podanym identyfikatorze i łączy go z kontrolerem symulacji.
     * @param id Unikalny identyfikator węzła.
     * @param controller Kontroler zarządzający całą symulacją.
     */
    public Node(int id, SimulationController controller) {
        this.id = id;
        this.controller = controller;
        this.crcUtil = new CRCUtil();
    }

    /** Dodaje sąsiada (połączenie dwukierunkowe w grafie). */
    public void addNeighbor(Node n) {
        neighbors.add(n);
    }

    /** Uruchamia wątek węzła i rozpoczyna jego działanie. */
    public void start() {
        nodeThread = new Thread(this, "Komputer-" + id);
        nodeThread.start();
    }

    /**
     * Wysyła pakiet danych do innego węzła.
     * Uwzględnia błędy, takie jak: błędy CRC, opóźnienia, utraty pakietów czy odwrócenie bitu.
     * @param target  Węzeł docelowy (odbiorca).
     * @param message Treść wiadomości do wysłania.
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
     * Odbiera pakiet i wykonuje odpowiednie działania w zależności od jego typu.
     * <ul>
     *   <li>Jeśli to pakiet ACK – potwierdza lub żąda retransmisji.</li>
     *   <li>Jeśli to pakiet danych – weryfikuje poprawność CRC.</li>
     * </ul>
     * @param packet Pakiet do przetworzenia.
     * @throws InterruptedException W przypadku przerwania wątku podczas oczekiwania.
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


    /**
     * Główna pętla wątku węzła.
     * Oczekuje na pakiety w kolejce i przetwarza je sekwencyjnie.
     */
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

    /** Wyszukuje sąsiada po identyfikatorze. */
    private Node findNeighborById(int id) {
        for (Node n : neighbors) {
            if (n.getId() == id) return n;
        }
        return null;
    }

    /** Wykonuje losową inwersję bitu w danych, symulując błąd transmisji.
     *  W tym przypadku zmienia tylko bity końcowe (CRC), aby pokazać działanie w logach.
     */
    private String flipRandomBit(String data, String polynomial) {
        if (data == null || data.isEmpty()) return data;
        Random random = new Random();
        int index = random.nextInt(data.length()-polynomial.length() + 1, data.length());
        char[] bits = data.toCharArray();
        bits[index] = (bits[index] == '0') ? '1' : '0';
        return new String(bits);
    }

    /**
     * Naprawia aktualny błąd i przywraca normalne działanie węzła.
     * Aktualizuje stan wizualny w interfejsie.
     */
    public void repairFault() {
        if (currentFault != null) {
            Logger.log("Komputer " + id + ": usterka " + currentFault.getType() + " usunięta.");
            currentFault.deactivate();
            isActive = true;
            currentFault = null;
            controller.getGraphPanel().updateNodeStatus(this);
        }
    }

    /**
     * Wstrzykuje błąd określonego typu do węzła.
     * Zmienia jego stan.
     * @param type Typ błędu do wstrzyknięcia.
     */
    public void injectFault(ErrorType type) {
        this.currentFault = new Fault(type);
        this.isActive = type != ErrorType.NODE_FREEZE;
        Logger.log("Komputer " + id + ": wstrzyknięto błąd " + type);
    }

    /** Ustawia wielomian używany do obliczania CRC. */
    public void setCrcPolynomial(String polynomial) {
        this.polynomial = polynomial;
    }

    /** Aktualizuje dane statystyczne w interfejsie użytkownika. */
    private void updateStatsInUI() {
        if (controller != null && controller.getGraphPanel() != null)
            controller.getGraphPanel().updateNodeStats(this);
    }

    /** @return Identyfikator węzła. */
    public int getId() { return id; }

    /** @return Kolejka przychodzących pakietów. */
    public BlockingQueue<Packet> getQueue() { return incomingQueue; }

    /** @return Czy węzeł jest aktywny. */
    public boolean isActive() { return isActive; }

    /** @return Lista sąsiadów (połączonych węzłów). */
    public List<Node> getNeighbors() { return neighbors; }

    /** @return Aktualny błąd (usterka), jeśli występuje. */
    public Fault getCurrentFault() { return currentFault; }

    /** @return Tekstowy opis aktualnego błędu lub "brak" jeśli nieaktywny. */
    public String getCurrentErrorText() {
        return currentFault != null && currentFault.isActive() ? currentFault.getType().name() : "brak";
    }

    /** @return Liczba wykrytych błędów. */
    public int getErrorCount() { return errorCount; }

    /** @return Liczba wysłanych pakietów. */
    public int getSentCount() { return sentCount; }

    /** @return Liczba odebranych pakietów. */
    public int getReceivedCount() { return receivedCount; }
}