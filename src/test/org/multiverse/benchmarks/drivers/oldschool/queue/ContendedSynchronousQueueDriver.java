package org.multiverse.benchmarks.drivers.oldschool.queue;

import org.multiverse.benchmarkframework.executor.AbstractDriver;
import org.multiverse.benchmarkframework.executor.TestCase;

import java.util.concurrent.SynchronousQueue;

/**
 * A {@link org.multiverse.benchmarkframework.executor.Driver} for benchmarking a {@link java.util.concurrent.SynchronousQueue}.
 *
 * @author Peter Veentjer
 */
public class ContendedSynchronousQueueDriver extends AbstractDriver {

    private SynchronousQueue queue;
    private int consumerCounter;
    private int producerCount;

    @Override
    public void preRun(TestCase testCase){
        queue = new SynchronousQueue();
        producerCount = testCase.getIntProperty("producerCount");
        consumerCounter = testCase.getIntProperty("consumerCount");
    }

    @Override
    public void run() {
        //todo
    }
}
