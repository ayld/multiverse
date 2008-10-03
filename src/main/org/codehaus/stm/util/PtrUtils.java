package org.codehaus.stm.util;

import org.codehaus.stm.IllegalPointerException;

import static java.lang.String.format;

public final class PtrUtils {

    public static void checkPtrAndVersion(long ptr, long version) {
        checkPtr(ptr);
        checkVersion(version);
    }

    public static void checkVersion(long version) {
        if (version < 0)
            throw new IllegalArgumentException(format("Version must be equal or larger than 0, version was %d", version));
    }

    public static void checkPtr(long ptr) {
        if (ptr <= 0)
            throw new IllegalPointerException(format("Pointer must be larger than 0, ptr was %d", ptr));
    }

    private PtrUtils() {
    }
}
