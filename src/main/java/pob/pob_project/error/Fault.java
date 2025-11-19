package pob.pob_project.error;

/**
 * Reprezentuje usterkę (błąd) w węźle sieci.
 * Klasa przechowuje informacje o typie błędu ({@link ErrorType}) oraz o tym, czy usterka jest aktualnie aktywna.
 */
public class Fault {
    /** Typ błędu powiązany z tą usterką. */
    private ErrorType type;
    /** Flaga określająca, czy błąd jest aktualnie aktywny. */
    private boolean active;

    /**
     * Tworzy nową usterkę danego typu i automatycznie ją aktywuje.
     * @param type Typ błędu, który ma zostać wstrzyknięty.
     */
    public Fault(ErrorType type) {
        this.type = type;
        this.active = true;
    }

    /**
     * Dezaktywuje usterkę, przywracając normalne działanie węzła.
     */
    public void deactivate() {
        this.active = false;
    }

    /**
     * Zwraca typ błędu przypisany do tej usterki.
     * @return Typ błędu jako {@link ErrorType}.
     */
    public ErrorType getType() { return type; }

    /**
     * Sprawdza, czy usterka jest aktualnie aktywna.
     * @return Wartość {@code true} jeśli błąd jest aktywny, {@code false} w przeciwnym wypadku.
     */
    public boolean isActive() { return active; }
}