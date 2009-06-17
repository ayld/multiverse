package org.multiverse.benchmarks.drivers.oldschool.cas;

import org.junit.Test;
import org.multiverse.TestThread;
import static org.multiverse.TestUtils.joinAll;
import static org.multiverse.TestUtils.startAll;
import org.multiverse.benchmarkframework.executor.AbstractDriver;
import org.multiverse.benchmarkframework.executor.TestCase;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A Driver that tests how contended cas behaves.
 *
 * @author Peter Veentjer
 */
public class ContendedCasDriver extends AbstractDriver {

    private IncrementAndGetThread[] threads;
    private AtomicLong cas;
    private int threadCount;
    private long count;

    @Override
    public void preRun(TestCase testCase) {
        cas = new AtomicLong();
        threadCount = testCase.getIntProperty("threadCount");
        count = testCase.getLongProperty("count");

        threads = new IncrementAndGetThread[threadCount];
        for (int k = 0; k < threadCount; k++) {
            threads[k] = new IncrementAndGetThread(k);
        }
    }

    @Override
    public void run() {
        startAll(threads);
        joinAll(threads);
    }

    private class IncrementAndGetThread extends TestThread {

        public IncrementAndGetThread(int id) {
            super("IncrementAndGetThread-" + id);
        }

        @Test
        public void run() {
            for (long k = 0; k < count; k++) {
                cas.incrementAndGet();
            }
        }
    }
}
