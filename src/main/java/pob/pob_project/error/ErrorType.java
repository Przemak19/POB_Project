package pob.pob_project.error;

/**
 * Typ wyliczeniowy z wstrzykiwanymi błędami.
 */
public enum ErrorType {
    BIT_FLIP,        // zmiana bitu danych
    PACKET_DROP,     // utrata pakietu
    NODE_FREEZE,     // zatrzymanie węzła
    DELAY,           // opóźnienie transmisji
    CRC_FAILURE      // awaria modułu CRC
}