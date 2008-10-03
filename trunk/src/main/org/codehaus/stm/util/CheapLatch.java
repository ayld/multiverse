package org.codehaus.stm.util;

import java.util.concurrent.TimeUnit;

/**
 * A {@link Latch} based on the intrinsic lock. It can't do any timed waits.
 *
 * @author Peter Veentjer.
 */
public final class CheapLatch implements Latch {

    private volatile boolean isOpen = false;

    public void await() throws InterruptedException {
        if (isOpen)
            return;

        synchronized (this) {
            while (!isOpen)
                wait();
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
     */
    public void tryAwait(long timeout, TimeUnit unit) throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    public boolean isOpen() {
        return isOpen;
    }
}
