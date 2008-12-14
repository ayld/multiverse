package org.codehaus.multiverse.util;

import org.codehaus.multiverse.core.NoSuchObjectException;
import org.codehaus.multiverse.multiversionedstm.StmObject;

import static java.lang.String.format;

public final class HandleUtils {

    //todo: this method doesn't belong in this package
    public static long getHandle(StmObject object) {
        return object == null ? 0 : object.___getHandle();
    }

    public static void assertValidVersion(long version) {
        if (!isValidVersion(version))
            throw new IllegalArgumentException(format("Version must be equal or larger than 0, version was %d", version));
    }

    public static boolean isValidVersion(long version) {
        return version >= 0;
    }

    public static void assertNotNull(long handle) {
        if (handle == 0)
            throw new NoSuchObjectException("Handle can't be 0");
    }

    private HandleUtils() {
    }
}
