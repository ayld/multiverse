package org.codehaus.multiverse.multiversionedstm;

public final class StmObjectUtils {

    public static long getHandle(StmObject object) {
        return object == null ? 0 : object.___getHandle();
    }

    private StmObjectUtils() {
    }
}
