package org.codehaus.multiverse.multiversionedstm;

public final class StmObjectUtils {

    public static long getHandle(StmObject object) {
        return object == null ? 0 : object.___getHandle();
    }

    public static boolean equals(Object o1, Object o2) {
        return o1 == null ? o2 != null : o1.equals(o2);
    }

    public static boolean hasHandle(long handle, UnloadedHolder holder) {
        if (handle == 0) {
            return holder == null;
        } else if (holder == null) {
            return false;
        }

        return holder.getHandle() == handle;
    }

    private StmObjectUtils() {
    }
}
