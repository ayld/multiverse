package org.codehaus.multiverse.utils;

import static java.lang.String.format;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Statistics that can be used for non blocking algorithms. In some cases a non blocking algorithm needs to
 * retry untill it succeeds. This could lead to a livelock. This NonBlockingStatistics can be used to track
 * down problems (if the #getFailurePercentage is high, the system could be in a livelock).
 *
 * @author Peter Veentjer.
 */
public final class NonBlockingStatistics {

    private final AtomicLong enterCount = new AtomicLong();
    private final AtomicLong failureCount = new AtomicLong();
    private volatile String name;

    public NonBlockingStatistics() {
    }

    public NonBlockingStatistics(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getEnterCount() {
        return enterCount.longValue();
    }

    public void incEnterCount() {
        enterCount.incrementAndGet();
    }

    public long getFailureCount() {
        return failureCount.longValue();
    }

    public void incFailureCount() {
        failureCount.incrementAndGet();
    }

    public double getFailurePercentage() {
        long enterCountLocal = enterCount.get();
        long failureCountLocal = failureCount.get();
        long total = enterCountLocal + failureCountLocal;

        if (enterCountLocal == 0)
            return 0;

        return (100.0d * failureCountLocal) / total;
    }

    public void renderAsString(StringBuffer sb) {
        String nameLocal = name;

        if (nameLocal == null) {
            sb.append(format("enter.count %s \n", enterCount.longValue()));
            sb.append(format("failure.count count %s \n", enterCount.longValue()));
            sb.append(format("failure.percentage %2f\n", getFailurePercentage()));
        } else {
            sb.append(format("%s.enter.count %s \n", nameLocal, enterCount.longValue()));
            sb.append(format("%s.failure.count %s \n", nameLocal, failureCount.longValue()));
            sb.append(format("%s.failure.percentage %2f\n", nameLocal, getFailurePercentage()));
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        renderAsString(sb);
        return sb.toString();
    }
}
