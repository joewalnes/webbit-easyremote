package org.webbitserver.easyremote.outbound;

import org.webbitserver.easyremote.NotCsvSerializableException;

/**
 * This implementation just comma-separates the function name
 * and the arguments. This is faster than serialising/deserialising JSON.
 *
 * Using this class you can only send primitive types and strings. Strings cannot contain a comma.
 * No nested types or other objects either. You're trading flexibility for speed with this class.
 * If you need to send more complex data, use {@link GsonClientMaker} instead.
 */
public class CsvClientMaker extends DynamicProxyClientMaker {
    @Override
    protected String createMessage(String methodName, Object[] args) {
        StringBuilder msg = new StringBuilder(methodName);
        for (Object arg : args) {
            checkSerializable(arg);
            msg.append(',').append(arg);
        }
        return msg.toString();
    }

    private void checkSerializable(Object arg) {
        if(null == arg) {
            return;
        }
        if(Number.class.isAssignableFrom(arg.getClass())) {
            return;
        }
        if(String.class.equals(arg.getClass()) && ((String) arg).indexOf(',') == -1) {
            return;
        }
        if(Boolean.class.isAssignableFrom(arg.getClass())) {
            return;
        }
        throw new NotCsvSerializableException(arg);
    }
}
