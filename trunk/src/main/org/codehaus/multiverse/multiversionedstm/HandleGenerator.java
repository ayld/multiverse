package org.codehaus.multiverse.multiversionedstm;

import java.util.concurrent.atomic.AtomicLong;

/**
 * The HandleGenerator is responsible for generating a handle for {@link StmObject} instances.
 * <p/>
 * The {@link HandleGenerator#createHandle()} is threadsafe to call. Internally it uses an {@link AtomicLong} for
 * creating handles. So no locking required if the platform supports cas instructions (and most do).
 *
 * @author Peter Veentjer.
 */
public final class HandleGenerator {

    private final static AtomicLong counter = new AtomicLong();

    /**
     * Creates a unique non 0 handle.
     *
     * @return a unique non 0 handle.
     */
    public static long createHandle() {
        return counter.incrementAndGet();
    }
}
