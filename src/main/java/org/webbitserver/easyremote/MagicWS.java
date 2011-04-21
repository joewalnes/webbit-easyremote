package org.webbitserver.easyremote;

import com.google.gson.Gson;
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
        Gson gson = new Gson();
        this.in = new GsonInboundDispatcher(server, clientType, gson);
        this.server = server;
    }

    public static <T> WebSocketHandler magic(Class<T> clientType, Server<T> server) {
        return new MagicWS<T>(clientType, server);
    }

    @Override
    public void onOpen(WebSocketConnection connection) throws Exception {
        String format = connection.httpRequest().queryParam("format");
        ClientMaker clientMaker = "csv".equals(format) ? new CsvClientMaker() : new GsonClientMaker();
        CLIENT client = clientMaker.implement(clientType, connection);
        ((Exporter)client).__exportMethods(in.availableMethods());
        connection.data(CLIENT_KEY, client);
        server.onOpen(connection, client);
    }

    @Override
    public void onMessage(WebSocketConnection connection, String msg) throws Exception {
        in.dispatch(connection, msg, connection.data(CLIENT_KEY));
    }

    @Override
    public void onClose(WebSocketConnection connection) throws Exception {
        server.onClose(connection, (CLIENT) connection.data(CLIENT_KEY));
    }
}
