package pob.pob_project.error;

import pob.pob_project.network.Node;

public class ErrorInjector {

    public void applyError(Node node, ErrorType type) {
        node.injectFault(type);
    }
}
