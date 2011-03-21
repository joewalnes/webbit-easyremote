package org.webbitserver.easyremote.outbound;

import org.webbitserver.WebSocketConnection;

public interface ClientMaker {
    <T> T implement(Class<T> type, WebSocketConnection connection);
}
