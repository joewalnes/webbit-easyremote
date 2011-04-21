package org.webbitserver.easyremote;

public class NoSuchRemoteMethodException extends RuntimeException {
    public NoSuchRemoteMethodException(String methodDescription) {
        super("No such remote method: " + methodDescription);
    }
}
