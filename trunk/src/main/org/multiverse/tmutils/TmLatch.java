package org.multiverse.tmutils;

import static org.multiverse.api.StmUtils.retry;
import org.multiverse.api.annotations.TmEntity;

import static java.lang.String.format;

/**
 * A Latch is a concurrency structure that could be seen as a door. While the door is closed,
 * every thread that wants to enter the door, needs to wait. Once the door is opened, all
 * waiting threads can continue and all threads that want to enter can enter immediately. Once
 * the door is opened, it can never be closed.
 *
 * @author Peter Veentjer.
 */
@TmEntity
public final class TmLatch {

    private boolean isOpen;

    /**
     * Creates a new closed Latch.
     */
    public TmLatch() {
        this.isOpen = false;
    }

    /**
     * Creates a new Latch.
     *
     * @param isOpen true if the Latch already is open, false if the Latch is still closed.
     */
    public TmLatch(boolean isOpen) {
        this.isOpen = isOpen;
    }

    /**
     * Waits till this Latch opens.
     */
    public void awaitOpen() {
        if (!isOpen) {
            retry();
        }
    }

    /**
     * Opens this Latch. If the Latch already is open, the call is ignored.
     */
    public void open() {
        isOpen = true;
    }

    /**
     * Checks if the Latch is open.
     *
     * @return true if the Latch is open, false otherwise.
     */
    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public String toString() {
        return format("Latch(isOpen=%s)", isOpen);
    }
}
