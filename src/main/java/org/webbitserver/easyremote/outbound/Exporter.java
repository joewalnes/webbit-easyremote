package org.webbitserver.easyremote.outbound;

import java.util.Set;

public interface Exporter {
    void __exportMethods(Set<String> methods);
}
