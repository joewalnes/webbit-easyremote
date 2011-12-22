package org.webbitserver.easyremote;

import org.webbitserver.WebSocketConnection;
import org.webbitserver.WebSocketHandler;
import org.webbitserver.easyremote.inbound.GsonInboundDispatcher;
import org.webbitserver.easyremote.inbound.InboundDispatcher;
import org.webbitserver.easyremote.outbound.ClientMaker;
import org.webbitserver.easyremote.outbound.CsvClientMaker;
import org.webbitserver.easyremote.outbound.Exporter;
import org.webbitserver.easyremote.outbound.GsonClientMaker;

@SuppressWarnings({"unchecked"})
public class MagicWS<CLIENT> implements WebSocketHandler {

    public static final String CLIENT_KEY = MagicWS.class.getPackage().getName() + ".client";

    private final Class<CLIENT> clientType;
    private final Server<CLIENT> server;
    private final InboundDispatcher in;

    public MagicWS(Class<CLIENT> clientType, Server<CLIENT> server) {
        this.clientType = clientType;
        this.in = new GsonInboundDispatcher(server, clientType);
        this.server = server;
    }

    public static <T> WebSocketHandler magic(Class<T> clientType, Server<T> server) {
        return new MagicWS<T>(clientType, server);
    }

    @Override
    public void onOpen(WebSocketConnection connection) throws Exception {
        String serverClientFormat = connection.httpRequest().queryParam("serverClientFormat");
        ClientMaker clientMaker = "csv".equals(serverClientFormat) ? new CsvClientMaker() : new GsonClientMaker();
        CLIENT client = clientMaker.implement(clientType, connection);
        ((Exporter)client).__exportMethods(in.availableMethods());
        connection.data(CLIENT_KEY, client);
        server.onOpen(connection, client);
    }

    @Override
    public void onMessage(WebSocketConnection connection, String msg) throws Throwable {
        in.dispatch(connection, msg, connection.data(CLIENT_KEY));
    }

    @Override
    public void onMessage(WebSocketConnection webSocketConnection, byte[] bytes) throws Throwable {
    }

    @Override
    public void onPong(WebSocketConnection webSocketConnection, String s) throws Throwable {
    }

    @Override
    public void onClose(WebSocketConnection connection) throws Exception {
        server.onClose(connection, (CLIENT) connection.data(CLIENT_KEY));
    }
}
