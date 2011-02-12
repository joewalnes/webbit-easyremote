package org.webbitserver.easyremote;

import org.webbitserver.WebSocketConnection;

public interface Client {
    WebSocketConnection connection();
}
