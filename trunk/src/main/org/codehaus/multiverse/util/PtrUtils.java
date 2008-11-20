package org.codehaus.multiverse.util;

import org.codehaus.multiverse.transaction.ObjectDoesNotExistException;

import static java.lang.String.format;

public final class PtrUtils {

    public static void checkHandleAndVersion(long handle, long version) {
        checkHandle(handle);
        checkVersion(version);
    }

    public static void checkVersion(long version) {
        if (version < 0)
            throw new IllegalArgumentException(format("Version must be equal or larger than 0, version was %d", version));
    }

    public static boolean versionIsValid(long version){
        return version>=0;
    }

    public static void checkHandle(long handle) {
        if (handle <= 0)
            throw new ObjectDoesNotExistException(format("Handle must be larger than 0, handle was %d", handle));
    }

    private PtrUtils() {
    }
}
