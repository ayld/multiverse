package org.multiverse.benchmarks.drivers.oldschool.queue;

import org.benchy.executor.TestCase;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * A {@link org.benchy.executor.Driver} for benchmarking the  {@link ArrayBlockingQueue}.
 *
 * @author Peter Veentjer
 */
public class ContendedArrayBlockingQueueDriver extends AbstractBlockingQueueDriver {

    private int capacity;
    private boolean fair;

    @Override
    public BlockingQueue createQueue() {
        return new ArrayBlockingQueue(capacity, fair);
    }

    @Override
    public void preRun(TestCase testCase) {
        capacity = testCase.getIntProperty("capacity");
        fair = testCase.getBooleanProperty("fair");
        super.preRun(testCase);
    }
}
