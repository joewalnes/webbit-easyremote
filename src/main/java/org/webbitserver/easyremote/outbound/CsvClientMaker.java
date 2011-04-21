package org.webbitserver.easyremote.outbound;

import org.webbitserver.easyremote.NotCsvSerializableException;

/**
 * This implementation just comma-separates the function name
 * and the arguments. This is faster than serialising/deserialising JSON.
 *
 * Using this class you can only send null, numbers, booleans, enums and strings without comma.
 * null becomes an empty string, which is falseley in javascript.
 *
 * You're trading flexibility for speed with this class.
 * If you need to send more complex data, use {@link GsonClientMaker} instead.
 */
public class CsvClientMaker extends DynamicProxyClientMaker {
    private static final String NULL = "";

    @Override
    protected String createMessage(String methodName, Object[] args) {
        StringBuilder msg = new StringBuilder(methodName);
        for (Object arg : args) {
            msg.append(',').append(format(arg));
        }
        return msg.toString();
    }

    private String format(Object arg) {
        if(null == arg) {
            return NULL;
        }
        if(Number.class.isAssignableFrom(arg.getClass())) {
            return String.valueOf(arg);
        }
        if(String.class.equals(arg.getClass()) && ((String) arg).indexOf(',') == -1) {
            return (String) arg;
        }
        if(Boolean.class.isAssignableFrom(arg.getClass())) {
            return String.valueOf(arg);
        }
        if(Enum.class.isAssignableFrom(arg.getClass())) {
            return String.valueOf(arg);
        }
        throw new NotCsvSerializableException(arg);
    }
}
