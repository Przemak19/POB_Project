package pob.pob_project.error;

/**
 * Typy błędów, które mogą wystąpić w węzłach sieci podczas symulacji.
 * Enum definiuje różne scenariusze awarii, które można wstrzyknąć.
 */
public enum ErrorType {

    /**
     * Błąd bitowy — losowa zmiana pojedynczego bitu w danych pakietu.
     */
    BIT_FLIP,

    /**
     * Utrata pakietu — pakiet nie zostaje dostarczony do celu.
     */
    PACKET_DROP,

    /**
     * Zamrożenie węzła — węzeł staje się nieaktywny i nie wysyła ani nie odbiera danych.
     */
    NODE_FREEZE,

    /**
     * Opóźnienie transmisji — pakiet jest dostarczany z losowym opóźnieniem czasowym.
     */
    DELAY,

    /**
     * Awaria modułu CRC — węzeł generuje błąd podczas obliczania CRC.
     */
    CRC_FAILURE
}