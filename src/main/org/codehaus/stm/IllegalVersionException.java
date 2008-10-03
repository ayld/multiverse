package org.codehaus.stm;

import static java.lang.String.format;

public class IllegalVersionException extends RuntimeException {

    public IllegalVersionException() {
    }

    public IllegalVersionException(long version) {
        super(format("Illegal version %s", version));
    }

    public IllegalVersionException(String msg) {
        super(msg);
    }
}
