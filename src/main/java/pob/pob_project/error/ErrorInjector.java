package pob.pob_project.error;

import pob.pob_project.network.Node;

/**
 * Klasa odpowiedzialna za wstrzykiwanie błędów (usterki) do węzłów sieci.
 * Umożliwia symulację różnych typów błędów, z typu wyliczeniowego ({@link ErrorType}).
 * Klasa ta jest wykorzystywana przez kontroler symulacji do testowania zachowania sieci w warunkach błędów.
 */
public class ErrorInjector {
    /**
     * Wstrzykuje wybrany typ błędu do podanego węzła.
     * @param node Węzeł, do którego ma zostać wprowadzony błąd.
     * @param type Typ błędu, który ma zostać wstrzyknięty.
     */
    public void applyError(Node node, ErrorType type) {
        node.injectFault(type);
    }
}
