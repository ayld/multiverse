package org.codehaus.multiverse.util;

import static java.lang.String.format;
import java.util.concurrent.atomic.AtomicLong;

/**
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

    public double getFailureRatio() {
        long enterCountLocal = enterCount.get();
        long failureCountLocal = failureCount.get();

        if (enterCountLocal == 0)
            return 0;

        return (1.0d * failureCountLocal) / enterCountLocal;
    }

    public void renderAsString(StringBuffer sb) {
        String nameLocal = name;

        if (nameLocal == null) {
            sb.append(format("enter.count %s \n", enterCount.longValue()));
            sb.append(format("failure.count count %s \n", enterCount.longValue()));
            sb.append(format("failure.ratio: %s\n", getFailureRatio()));
        } else {
            sb.append(format("%s.enter.count %s \n", nameLocal, enterCount.longValue()));
            sb.append(format("%s.failure.count %s \n", nameLocal, failureCount.longValue()));
            sb.append(format("%s.failure.ratio: %s\n", nameLocal, getFailureRatio()));
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        renderAsString(sb);
        return sb.toString();
    }
}
