package org.codehaus.multiverse.util;

import static java.lang.String.format;

public final class VersionedHandle {

    private final long handle;
    private final long version;

    public VersionedHandle(long handle, long version) {
        this.handle = handle;
        this.version = version;
    }

    public long getHandle() {
        return handle;
    }

    public long getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (!(o instanceof VersionedHandle))
            return false;

        VersionedHandle that = (VersionedHandle) o;
        return that.handle == this.handle && that.version == this.version;
    }

    @Override
    public int hashCode() {
        int result = (int) (handle ^ (handle >>> 32));
        result = 31 * result + (int) (version ^ (version >>> 32));
        return result;
    }

    public String toString() {
        return format("VersionedHandle(handle=%s, version=%s)", handle, version);
    }
}
