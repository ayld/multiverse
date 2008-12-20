package org.codehaus.multiverse.util.latches;

import java.util.concurrent.TimeUnit;

/**
 * A structure that can be used as a waiting point. As long as it is closed, the thread waits (unless it is
 * interrupted or a timeout occurs). As soon as it opens, all waiting threads can pass. Threads that call the
 * wait after the Latch is openen, can continue. Once the Latch has been opened, it can never be closed.
 *
 * A Latch is threadsafe.
 *
 * @author Peter Veentjer.
 */
public interface Latch {

    /**
     * Waits for this Latch to open. If the Latch already is open, the call continues.
     *
     * @throws InterruptedException if the waiting thread is interrupted.
     */
    void await() throws InterruptedException;

    /**
     * Waits for this Latch to open or till a timeout occurs.
     *
     * @param timeout
     * @param unit
     * @throws InterruptedException
     * @throws NullPointerException if  unit is null
     */
    void tryAwait(long timeout, TimeUnit unit)throws InterruptedException;

    /**
     * Opens the latch. If the latch already is open, the call is ignored.
     */
    void open();

    /**
     * Return true if this Latch is open, false othewise.
     *
     * @return true if this Latch is open, false otherwise.
     */
    boolean isOpen();
}
