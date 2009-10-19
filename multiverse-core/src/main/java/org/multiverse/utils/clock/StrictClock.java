package org.multiverse.utils.clock;

import static java.lang.String.format;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The intuitive implementation of a clock. It wraps an AtomicLong and increases the value every time a
 * tick is done.
 * <p/>
 * A strict clock provides a full ordering of all transactions (also transactions that don't shate state).
 * This full ordering causes contention on the memory bus. See the {@link RelaxedClock} for more info.
 *
 * @author Peter Veentjer.
 */
public final class StrictClock implements Clock {

    private final AtomicLong clock;

    private final long dawn;

    /**
     * Creates a clock with 0 as dawn time.
     */
    public StrictClock() {
        this(0);
    }

    /**
     * Creates a clock with the provided dawn time.
     *
     * @param dawn the dawn time.
     */
    public StrictClock(long dawn) {
        this.dawn = dawn;
        this.clock = new AtomicLong(dawn);
    }

    @Override
    public long getDawn() {
        return dawn;
    }

    @Override
    public long tick() {
        return clock.incrementAndGet();
    }

    @Override
    public long getTime() {
        return clock.get();
    }

    @Override
    public String toString() {
        return format("StrictClock(time=%s)", clock.get());
    }
}

