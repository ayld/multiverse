package org.codehaus.multiverse.utils.latches;

import static java.lang.String.format;
import java.util.concurrent.TimeUnit;

/**
 * A {@link Latch} based on the intrinsic lock and uses the minimal amount of resources. It uses the CheapLatch object
 * itself for the monitor lock. It can't do any timed waits.
 *
 * @author Peter Veentjer.
 */
@SuppressWarnings({"SynchronizeOnThis", "WaitOrAwaitWithoutTimeout"})
public final class CheapLatch implements Latch {

    public final static CheapLatch OPEN_LATCH = new CheapLatch(true);

    private volatile boolean isOpen;

    /**
     * Creates a new closed CheapLatch.
     */
    public CheapLatch() {
        this(false);
    }

    /**
     * Creates a new CheapLatch.
     *
     * @param isOpen true if the latch already is open, false if the latch is closed.
     */
    public CheapLatch(boolean isOpen) {
        this.isOpen = isOpen;
    }

    public void await() throws InterruptedException {
        if (isOpen)
            return;

        synchronized (this) {
            while (!isOpen)
                wait();
        }
    }

    @Override
    public void awaitUniterruptibly() {
        if (isOpen)
            return;

        boolean restoreInterrupt = false;

        try {
            synchronized (this) {
                while (!isOpen) {
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                        restoreInterrupt = true;
                    }
                }
            }
        } finally {
            if (restoreInterrupt)
                Thread.currentThread().interrupt();
        }
    }

    public void open() {
        if (isOpen)
            return;

        synchronized (this) {
            isOpen = true;
            notifyAll();
        }
    }

    /**
     * This operation is not supported on the CheapLatch.
     *
     * @throws UnsupportedOperationException because timed waits are not supported by this CheapLatch.
     */
    public void tryAwait(long timeout, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public String toString() {
        return format("CheapLatch(open=%s)", isOpen);
    }
}
