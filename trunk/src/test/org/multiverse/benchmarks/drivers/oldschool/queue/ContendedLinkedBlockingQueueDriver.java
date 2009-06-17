package org.multiverse.benchmarks.drivers.oldschool.queue;

import org.benchy.executor.TestCase;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A {@link org.benchy.executor.Driver} for benchmarking a {@link LinkedBlockingQueue}.
 * <p/>
 * todo:
 * also test with bound.
 * <p/>
 * todo:
 * if there is a difference in the number of producers/consumes the test is no good.
 *
 * @author Peter Veentjer
 */
public class ContendedLinkedBlockingQueueDriver extends AbstractBlockingQueueDriver {

    private int capacity;

    @Override
    public BlockingQueue createQueue() {
        return new LinkedBlockingQueue(capacity);
    }

    @Override
    public void preRun(TestCase testCase) {
        capacity = testCase.getIntProperty("capacity");
        super.preRun(testCase);
    }
}
