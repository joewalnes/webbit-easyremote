package org.webbitserver.easyremote.inbound;

import com.google.gson.Gson;
import org.webbitserver.HttpRequest;
import org.webbitserver.WebSocketConnection;
import org.webbitserver.easyremote.Remote;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class InboundDispatcher {

    public static class ActionArgsTuple {
        public String action;
        public Object[] args;
    }

    private final Gson gson;
    private final Object server;
    private final Class<?> clientType;
    private final Map<String, Method> serverMethods = new HashMap<String, Method>();

    public InboundDispatcher(Gson gson, Object server, Class<?> clientType) {
        this.gson = gson;
        this.server = server;
        this.clientType = clientType;
        for (Method method : server.getClass().getMethods()) {
            if (method.getAnnotation(Remote.class) != null) {
                serverMethods.put(method.getName(), method);
            }
        }
    }

    public void dispatch(WebSocketConnection connection, String msg, Object client) throws IllegalAccessException, InvocationTargetException {
        ActionArgsTuple map = gson.fromJson(msg, ActionArgsTuple.class);
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

    public Set<String> availableMethods() {
        return serverMethods.keySet();
    }
}
