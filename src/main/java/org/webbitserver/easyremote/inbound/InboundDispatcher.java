package org.webbitserver.easyremote.inbound;

import org.webbitserver.HttpRequest;
import org.webbitserver.WebSocketConnection;
import org.webbitserver.easyremote.BadNumberOfArgumentsException;
import org.webbitserver.easyremote.ClientException;
import org.webbitserver.easyremote.NoSuchRemoteMethodException;
import org.webbitserver.easyremote.Remote;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static java.util.Arrays.asList;

public abstract class InboundDispatcher {

    private final Map<String, Action> inboundActions = new HashMap<String, Action>();
    private final Class<?> clientType;

    public InboundDispatcher(final Object server, final Class<?> clientType) {
        this.clientType = clientType;
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
    public void __noSuchRemoteMethod(String methodDescription) {
        throw new NoSuchRemoteMethodException(methodDescription);
    }

    @Remote
    public void __badNumberOfArguments(String methodDescription, int declaredArguments, List<Object> invokedArguments) {
        throw new BadNumberOfArgumentsException(methodDescription, declaredArguments, invokedArguments);
    }

    @Remote
    public void __reportClientException(String message) {
        throw new ClientException(message);
    }

    protected abstract InboundMessage unmarshalInboundRequest(String msg);

    public interface InboundMessage {
        String method();

        Object[] args();
    }

    public void dispatch(WebSocketConnection connection, String msg, Object client) throws Throwable {
        InboundMessage inbound = unmarshalInboundRequest(msg);
        Action action = inboundActions.get(inbound.method());
        if (action == null) {
            // This is unlikely to happen since the client can only call exported methods. Keep it here as a saefeguard.
            throw new NoSuchRemoteMethodException(clientType.getName() + "." + inbound.method() + "([" + inbound.args().length + " args])");
        }
        action.call(connection, client, inbound.args());
    }

    public Set<String> availableMethods() {
        return inboundActions.keySet();
    }

    public static interface Action {
        void call(WebSocketConnection connection, Object client, Object[] args) throws Throwable;
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
        public void call(WebSocketConnection connection, Object client, Object[] args) throws Throwable {
            Class<?>[] paramTypes = method.getParameterTypes();
            Object[] callArgs = new Object[paramTypes.length];
            List<Object> argList = new ArrayList<Object>(asList(args));
            for (int i = 0; i < paramTypes.length; i++) {
                Class<?> paramType = paramTypes[i];
                if (paramType.isAssignableFrom(clientType)) {
                    callArgs[i] = client;
                } else if (paramType.isAssignableFrom(WebSocketConnection.class)) {
                    callArgs[i] = connection;
                } else if (paramType.isAssignableFrom(HttpRequest.class)) {
                    callArgs[i] = connection.httpRequest();
                } else {
                    try {
                        callArgs[i] = argList.remove(0);
                    } catch(IndexOutOfBoundsException e) {
                        throw new BadNumberOfArgumentsException(method.toString(), method.getParameterTypes().length, asList(args));
                    }
                }
            }
            if (argList.size() != 0) {
                //throw new BadNumberOfArgumentsException(method.toGenericString(), paramTypes.length, asList(args));
            }
            try {
                method.invoke(target, callArgs);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }
    }
}
