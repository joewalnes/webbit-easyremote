package org.webbitserver.easyremote;

import org.webbitserver.WebSocketConnection;

public interface Server<C> {

    void onOpen(WebSocketConnection connection, C client) throws Exception;

    void onClose(WebSocketConnection connection, C client) throws Exception;

}
