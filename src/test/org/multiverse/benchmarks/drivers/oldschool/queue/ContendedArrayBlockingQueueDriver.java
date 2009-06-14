package org.multiverse.benchmarks.drivers.oldschool.queue;

import org.multiverse.benchmarkframework.executor.AbstractDriver;
import org.multiverse.benchmarkframework.executor.TestCase;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * A {@link org.multiverse.benchmarkframework.executor.Driver} for benchmarking the  {@link ArrayBlockingQueue}.
 *
 * @author Peter Veentjer
 */
public class ContendedArrayBlockingQueueDriver extends AbstractDriver {

    private ArrayBlockingQueue queue;
    private int producerCount;
    private int consumerCount;

    @Override
    public void preRun(TestCase testCase) {
        producerCount = testCase.getIntProperty("producerCount");
        consumerCount = testCase.getIntProperty("consumerCount");
    }

    @Override
    public void run() {
        //todo
    }
}
