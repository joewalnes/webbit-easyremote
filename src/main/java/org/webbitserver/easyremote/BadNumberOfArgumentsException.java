package org.webbitserver.easyremote;

import java.util.List;

public class BadNumberOfArgumentsException extends RuntimeException {
    public BadNumberOfArgumentsException(String methodDescription, int declaredArguments, List<Object> invokedArguments) {
        super(methodDescription + " declared with " + declaredArguments + " arguments, can't be invoked with " + invokedArguments);
    }
}
