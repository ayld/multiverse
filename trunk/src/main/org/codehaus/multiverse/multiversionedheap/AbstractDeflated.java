package org.codehaus.multiverse.multiversionedheap;

import org.codehaus.multiverse.util.iterators.PLongArrayIterator;
import org.codehaus.multiverse.util.iterators.PLongIterator;

/**
 * Convenience implementation of the {@link Deflated}.
 *
 * @author Peter Veentjer
 */
public abstract class AbstractDeflated implements Deflated {

    private final long handle;
    private final long version;

    public AbstractDeflated(long handle, long version) {
        assert handle != 0;
        this.version = version;
        this.handle = handle;
    }

    public PLongIterator ___memberHandles() {
        return new PLongArrayIterator();
    }

    public long ___getVersion() {
        return version;
    }

    public long ___getHandle() {
        return handle;
    }
}
