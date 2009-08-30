package org.multiverse.utils.spinning;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A {@link org.multiverse.utils.spinning.SpinPolicy} that uses bounded spinning.
 * Unbounded spinning is very dangerous:
 * - the locks that already held prevent other transactions from completing
 * -
 *
 * @author Peter Veentjer.
 */
public final class BoundedSpinPolicy implements SpinPolicy {

    public final static AtomicLong spinAttempts = new AtomicLong();
    public final static AtomicLong spinSuccess = new AtomicLong();

    public final static BoundedSpinPolicy INSTANCE = new BoundedSpinPolicy(100);
    public final static BoundedSpinPolicy NO_SPIN = new BoundedSpinPolicy(0);

    private final int maxIterations;

    public BoundedSpinPolicy(int maxIterations) {
        if (maxIterations < 0) {
            throw new IllegalArgumentException();
        }
        this.maxIterations = maxIterations;
    }

    public int getMaxIterations() {
        return maxIterations;
    }

    @Override
    public boolean execute(SpinTask task) {
        if (task == null) {
            throw new NullPointerException();
        }

        spinAttempts.incrementAndGet();

        for (int iteration = 0; iteration < maxIterations; iteration++) {
            boolean success = task.run();
            if (success) {
                spinSuccess.incrementAndGet();
                return true;
            }
        }

        return false;
    }
}
