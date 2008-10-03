package org.codehaus.stm;

import static java.lang.String.format;

public class IllegalPointerException extends RuntimeException {

    private static String toMessage(long pointer) {
        if (pointer < 0) {
            return format("Pointer can't be negative, pointer was %d", pointer);
        } else if (pointer == 0) {
            return format("Null Pointer");
        } else {
            return format("Cell at address %d does not exist", pointer);
        }
    }

    public IllegalPointerException() {
    }

    public IllegalPointerException(long pointer) {
        super(toMessage(pointer));
    }

    public IllegalPointerException(String s) {
        super(s);
    }
}
