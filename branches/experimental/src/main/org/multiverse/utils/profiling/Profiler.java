package org.multiverse.utils.profiling;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Perhaps not needed to tell, but a Profiler should have the lowest amount of performance overhead
 * if possible.
 *
 * @author Peter Veentjer.
 */
public interface Profiler {

    /**
     * Implementations should watch out with string concatenation.
     *
     * @param transactionFamilyName
     * @param reason
     */
    AtomicLong getCounter(String transactionFamilyName, String reason);

    void reset();

    String getProfileInfo();
}
