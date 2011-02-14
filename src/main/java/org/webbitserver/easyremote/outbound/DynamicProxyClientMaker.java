package org.webbitserver.easyremote.outbound;

import org.webbitserver.WebSocketConnection;
import org.webbitserver.easyremote.Client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Generates an implementation of an outbound interface using a dynamic proxy.
 */
public abstract class DynamicProxyClientMaker implements ClientMaker {

    protected abstract String createMessage(Method method, Object[] args);

    @Override
    @SuppressWarnings({"unchecked"})
    public <T> T implement(Class<T> type, WebSocketConnection connection) {
        return (T) Proxy.newProxyInstance(classLoader(),
                new Class<?>[]{type},
                createInvocationHandler(type, connection));
    }

    protected ClassLoader classLoader() {
        return getClass().getClassLoader();
    }

    @SuppressWarnings({"UnusedDeclaration"}) // Type not used here, but made available to subclasses.
    protected InvocationHandler createInvocationHandler(final Class<?> type,
                                                        final WebSocketConnection connection) {
        return new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.getDeclaringClass() == Object.class) {
                    return method.invoke(connection, args);
                } else {
                    String msg = createMessage(method, args);
                    connection.send(msg);
                    return null;
                }
            }
        };
    }


}
