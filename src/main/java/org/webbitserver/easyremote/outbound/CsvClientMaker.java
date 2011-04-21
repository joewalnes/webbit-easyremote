package org.webbitserver.easyremote.outbound;

import java.lang.reflect.Method;

/**
 * This implementation just comma-separates the function name
 * and the arguments. This is faster than serialising/deserialising JSON.
 */
public class CsvClientMaker extends DynamicProxyClientMaker {
    @Override
    protected String createMessage(Method method, Object[] args) {
        StringBuilder msg = new StringBuilder(method.getName());
        for (Object arg : args) {
            msg.append(',').append(arg);
        }
        return msg.toString();
    }
}
