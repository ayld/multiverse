package org.multiverse.util;

import static java.lang.String.format;

/**
 * A counter that can be used to limit the number of retries when doing CAS-loops (compare and swap).
 *
 * @author Peter Veentjer.
 */
public final class RetryCounter {

    private int value;

    public RetryCounter(int value) {
        this.value = value;
    }

    /**
     * Decreases the RetryCounter.
     *
     * @return true if another attempt can be done, false otherwise.
     */
    public boolean decrease() {
        if (value <= 0)
            return false;

        value--;
        return true;
    }

    @Override
    public String toString() {
        return format("RetryCounter(value=%s)", value);
    }
}
