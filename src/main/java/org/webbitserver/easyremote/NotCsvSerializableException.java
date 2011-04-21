package org.webbitserver.easyremote;

public class NotCsvSerializableException extends RuntimeException {
    public NotCsvSerializableException(Object o) {
        super("Only null, numbers, booleans, enums and strings without comma can be serialized to CSV: " + o);
    }
}
