package org.webbitserver.easyremote;

import org.webbitserver.CometConnection;

public interface Server<C> {

    void onOpen(CometConnection connection, C client) throws Exception;

    void onClose(CometConnection connection, C client) throws Exception;

}
