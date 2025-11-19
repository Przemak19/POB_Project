package pob.pob_project.network;

import java.util.Random;

/**
 * Reprezentuje pakiet danych przesyłany pomiędzy komputerami (węzłami) w sieci.
 * Pakiet może zawierać dane użytkownika lub być pakietem potwierdzenia (ACK).
 * Dodatkowo może posiadać opóźnienie, co pozwala symulować warunki sieciowe, takie jak spowolnienie transmisji.
 */
public class Packet {
    /** Dane zawarte w pakiecie (ciąg bitów lub tekst). */
    private String data;

    /** Identyfikator źródła (nadawcy pakietu). */
    private final int sourceId;

    /** Identyfikator miejsca docelowego (odbiorcy pakietu). */
    private final int destinationId;

    /** Czy pakiet jest aktualnie opóźniony w transmisji. */
    private boolean isDelayed;

    /** Czas opóźnienia pakietu w milisekundach. */
    private int delay;

    /** Typ potwierdzenia: true – ACK pozytywny, false – żądanie retransmisji (NACK). */
    private boolean ackPositive = false;

    /** Czy pakiet jest potwierdzeniem (ACK). */
    private boolean isAck = false;

    /** Generator losowy, potencjalnie wykorzystywany do symulacji losowych zjawisk. */
    private static final Random random = new Random();


    /**
     * Tworzy nowy pakiet z danymi przesyłanymi między dwoma węzłami.
     * @param data          Zawartość pakietu (np. dane lub wiadomość).
     * @param sourceId      Identyfikator węzła nadawcy.
     * @param destinationId Identyfikator węzła odbiorcy.
     */
    public Packet(String data, int sourceId, int destinationId) {
        this.data = data;
        this.sourceId = sourceId;
        this.destinationId = destinationId;
        this.delay = 200;
    }

    /**
     * Tworzy specjalny pakiet typu ACK (potwierdzenie lub NACK).
     * @param sourceId      Identyfikator węzła wysyłającego potwierdzenie.
     * @param destinationId Identyfikator odbiorcy potwierdzenia.
     * @param ackPositive   Wartość {@code true} - ACK pozytywny, {@code false} - żądanie retransmisji.
     * @return Nowy pakiet potwierdzający.
     */
    public static Packet createAckPacket(int sourceId, int destinationId, boolean ackPositive) {
        Packet ack = new Packet("ACK", sourceId, destinationId);
        ack.isAck = true;
        ack.ackPositive = ackPositive;
        return ack;
    }

    /**
     * Ustawia, czy pakiet ma być opóźniony w transmisji.
     * @param isDelayed Wartość {@code true}, jeśli pakiet powinien być opóźniony.
     */
    public void setIsDelayed(boolean isDelayed) { this.isDelayed = isDelayed; }

    /** @return Czy pakiet jest pakietem typu ACK. */
    public boolean isAck() { return isAck; }

    /** @return Czy ACK jest {@code true} czy {@code false}. */
    public boolean isAckPositive() { return ackPositive; }

    /** Ustawia typ pakietu (ACK lub zwykły). */
    public void setAck(boolean ack) { isAck = ack; }

    /** Ustawia typ potwierdzenia ACK: {@code true} lub {@code false}. */
    public void setAckPositive(boolean ackPositive) { this.ackPositive = ackPositive; }

    /** @return Dane zawarte w pakiecie. */
    public String getData() { return data; }

    /** @return Identyfikator węzła nadawcy. */
    public int getSourceId() { return sourceId; }

    /** @return Identyfikator węzła odbiorcy. */
    public int getDestinationId() { return destinationId; }

    /** @return Czy pakiet jest aktualnie opóźniony. */
    public boolean isDelayed() { return isDelayed; }

    /**
     * Ustawia opóźnienie pakietu w milisekundach.
     * @param delay Czas opóźnienia w ms.
     */
    public void setDelay(int delay) { this.delay = delay; }

    /** @return Aktualne opóźnienie pakietu w milisekundach. */
    public int getDelay() { return delay; }
}
