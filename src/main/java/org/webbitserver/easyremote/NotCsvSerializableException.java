package org.webbitserver.easyremote;

public class NotCsvSerializableException extends RuntimeException {
    public NotCsvSerializableException(Object o) {
        super("Only null, numbers, booleans and strings without comma can be serialized to CSV: " + o);
    }
}
