package org.webbitserver.easyremote.inbound;

import org.webbitserver.HttpRequest;
import org.webbitserver.CometConnection;
import org.webbitserver.easyremote.Remote;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class InboundDispatcher {

    private final Map<String, Action> inboundActions = new HashMap<String, Action>();

    public InboundDispatcher(final Object server, final Class<?> clientType) {
        buildActionMap(server, clientType);
        buildActionMap(this, clientType);
    }

    private void buildActionMap(Object target, Class<?> clientType) {
        for (final Method method : target.getClass().getMethods()) {
            if (method.getAnnotation(Remote.class) != null) {
                inboundActions.put(method.getName(), new ReflectiveAction(method, clientType, target));
            }
        }
    }

    @Remote
    public void __noSuchFunction(String message) {
        throw new RuntimeException(message);
    }

    @Remote
    public void __badNumberOfArguments(String message) {
        throw new RuntimeException(message);
    }

    protected abstract InboundMessage unmarshalInboundRequest(String msg);

    public interface InboundMessage {
        String method();
        Object[] args();
    }

    public void dispatch(CometConnection connection, String msg, Object client) throws Exception {
        InboundMessage map = unmarshalInboundRequest(msg);
        Action action = inboundActions.get(map.method());
        action.call(connection, client, map.args());
    }

    public Set<String> availableMethods() {
        return inboundActions.keySet();
    }

    public static interface Action {
        void call(CometConnection connection, Object client, Object[] args) throws Exception;
    }

    private static class ReflectiveAction implements Action {
        private final Method method;
        private final Class<?> clientType;
        private final Object target;

        public ReflectiveAction(Method method, Class<?> clientType, Object target) {
            this.method = method;
            this.clientType = clientType;
            this.target = target;
        }

        @Override
        public void call(CometConnection connection, Object client, Object[] args) throws Exception {
            Class<?>[] paramTypes = method.getParameterTypes();
            Object[] callArgs = new Object[paramTypes.length];
            int argIndex = 0;
            for (int i = 0; i < paramTypes.length; i++) {
                Class<?> paramType = paramTypes[i];
                if (paramType.isAssignableFrom(clientType)) {
                    callArgs[i] = client;
                } else if (paramType.isAssignableFrom(CometConnection.class)) {
                    callArgs[i] = connection;
                } else if (paramType.isAssignableFrom(HttpRequest.class)) {
                    callArgs[i] = connection.httpRequest();
                } else {
                    callArgs[i] = args[argIndex++];
                }
            }
            method.invoke(target, callArgs);
        }
    }
}
