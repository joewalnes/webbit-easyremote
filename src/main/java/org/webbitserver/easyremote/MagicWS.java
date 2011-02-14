package org.webbitserver.easyremote;

import com.google.gson.Gson;
import org.webbitserver.HttpRequest;
import org.webbitserver.WebSocketConnection;
import org.webbitserver.WebSocketHandler;
import org.webbitserver.easyremote.outbound.ClientMaker;
import org.webbitserver.easyremote.outbound.GsonClientMaker;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"unchecked"})
public class MagicWS<C> implements WebSocketHandler {

    private final Class<C> clientType;
    private final Server<C> server;
    private final Map<String, Method> serverMethods = new HashMap<String, Method>();
    private final Gson gson = new Gson();
    private final ClientMaker clientMaker = new GsonClientMaker(gson);

    public MagicWS(Class<C> clientType, Server<C> server) {
        this.clientType = clientType;
        if (!clientType.isInterface()) {
            throw new IllegalArgumentException(clientType.getName() + " is not an interface");
        }
        if (clientType.getAnnotation(Remote.class) == null) {
            throw new IllegalArgumentException("Interface " + clientType.getName() + " not marked with " + Remote.class.getName() + " annotation");
        }
        this.server = server;
        for (Method method : server.getClass().getMethods()) {
            if (method.getAnnotation(Remote.class) != null) {
                serverMethods.put(method.getName(), method);
            }
        }
    }

    @Override
    public void onOpen(WebSocketConnection connection) throws Exception {
        exportMethods(connection);
        C client = clientMaker.implement(clientType, connection);
        connection.data("client", client);
        server.onOpen(connection, client);
    }

    public static <T> WebSocketHandler magic(Class<T> clientType, Server<T> server) {
        return new MagicWS<T>(clientType, server);
    }

    public static class Foo {
        public String action;
        public Object[] args;
    }

    private void exportMethods(WebSocketConnection connection) {
        Map<String, Object> r = new HashMap<String, Object>();
        r.put("exports", serverMethods.keySet());
        connection.send(gson.toJson(r));
    }

    @Override
    public void onMessage(WebSocketConnection connection, String msg) throws Exception {
        C client = (C) connection.data("client");
        Foo map = gson.fromJson(msg, Foo.class);
        Method method = serverMethods.get(map.action);

        Class<?>[] paramTypes = method.getParameterTypes();
        Object[] args = new Object[paramTypes.length];
        int argIndex = 0;
        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> paramType = paramTypes[i];
            if (paramType.isAssignableFrom(clientType)) {
                args[i] = client;
            } else if (paramType.isAssignableFrom(WebSocketConnection.class)) {
                args[i] = connection;
            } else if (paramType.isAssignableFrom(HttpRequest.class)) {
                args[i] = connection.httpRequest();
            } else {
                args[i] = map.args[argIndex++];
            }
        }
        method.invoke(server, args);
    }

    @Override
    public void onClose(WebSocketConnection connection) throws Exception {
        C client = (C) connection.data("client");
        server.onClose(connection,  client);
    }
}
