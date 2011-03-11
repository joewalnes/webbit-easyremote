package org.webbitserver.easyremote.outbound;

import org.webbitserver.CometConnection;

public interface ClientMaker {
    <T> T implement(Class<T> type, CometConnection connection);
}
