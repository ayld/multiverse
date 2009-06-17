package org.multiverse.benchmarks.drivers.oldschool.queue;

import org.benchy.executor.TestCase;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

public class ContendedSynchronousQueueDriver extends AbstractBlockingQueueDriver {
    private boolean fair;

    @Override
    public BlockingQueue createQueue() {
        return new SynchronousQueue(fair);
    }

    @Override
    public void preRun(TestCase testCase) {
        fair = testCase.getBooleanProperty("fair");
        super.preRun(testCase);
    }
}
