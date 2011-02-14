package org.webbitserver.easyremote.inbound;

import com.google.gson.Gson;
import org.webbitserver.HttpRequest;
import org.webbitserver.WebSocketConnection;
import org.webbitserver.easyremote.Remote;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class InboundDispatcher {

    private final Gson gson;
    private final Map<String, Action> inboundActions = new HashMap<String, Action>();

    public InboundDispatcher(Gson gson, final Object server, final Class<?> clientType) {
        this.gson = gson;

        for (final Method method : server.getClass().getMethods()) {
            if (method.getAnnotation(Remote.class) != null) {
                inboundActions.put(method.getName(), new ReflectiveAction(method, clientType, server));
            }
        }
    }

    private static class ActionArgsTuple {
        public String action;
        public Object[] args;
    }

    public void dispatch(WebSocketConnection connection, String msg, Object client) throws Exception {
        ActionArgsTuple map = gson.fromJson(msg, ActionArgsTuple.class);
        Action action = inboundActions.get(map.action);
        action.call(connection, client, map.args);
    }

    public Set<String> availableMethods() {
        return inboundActions.keySet();
    }

    private static interface Action {
        void call(WebSocketConnection connection, Object client, Object[] args) throws Exception;
    }

    private static class ReflectiveAction implements Action {
        private final Method method;
        private final Class<?> clientType;
        private final Object server;

        public ReflectiveAction(Method method, Class<?> clientType, Object server) {
            this.method = method;
            this.clientType = clientType;
            this.server = server;
        }

        @Override
        public void call(WebSocketConnection connection, Object client, Object[] args) throws Exception {
            Class<?>[] paramTypes = method.getParameterTypes();
            Object[] callArgs = new Object[paramTypes.length];
            int argIndex = 0;
            for (int i = 0; i < paramTypes.length; i++) {
                Class<?> paramType = paramTypes[i];
                if (paramType.isAssignableFrom(clientType)) {
                    callArgs[i] = client;
                } else if (paramType.isAssignableFrom(WebSocketConnection.class)) {
                    callArgs[i] = connection;
                } else if (paramType.isAssignableFrom(HttpRequest.class)) {
                    callArgs[i] = connection.httpRequest();
                } else {
                    callArgs[i] = args[argIndex++];
                }
            }
            method.invoke(server, callArgs);
        }
    }
}
