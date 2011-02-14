package org.webbitserver.easyremote;

import com.google.gson.Gson;
import org.webbitserver.WebSocketConnection;
import org.webbitserver.WebSocketHandler;
import org.webbitserver.easyremote.inbound.InboundDispatcher;
import org.webbitserver.easyremote.outbound.ClientMaker;
import org.webbitserver.easyremote.outbound.GsonClientMaker;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"unchecked"})
public class MagicWS<CLIENT> implements WebSocketHandler {

    public static final String CLIENT_KEY = MagicWS.class.getPackage().getName() + ".client";

    private final Class<CLIENT> clientType;
    private final Server<CLIENT> server;
    private final ClientMaker clientMaker;
    private final InboundDispatcher in;
    private final Gson gson;

    public MagicWS(Class<CLIENT> clientType, Server<CLIENT> server) {
        this.clientType = clientType;
        gson = new Gson();
        this.in = new InboundDispatcher(gson, server, clientType);
        this.clientMaker = new GsonClientMaker(gson);
        this.server = server;
    }

    public static <T> WebSocketHandler magic(Class<T> clientType, Server<T> server) {
        return new MagicWS<T>(clientType, server);
    }

    @Override
    public void onOpen(WebSocketConnection connection) throws Exception {
        exportMethods(connection);
        CLIENT client = clientMaker.implement(clientType, connection);
        connection.data(CLIENT_KEY, client);
        server.onOpen(connection, client);
    }

    private void exportMethods(WebSocketConnection connection) {
        Map<String, Object> r = new HashMap<String, Object>();
        r.put("exports", in.availableMethods());
        connection.send(gson.toJson(r));
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
