package org.multiverse.benchmarks.drivers.oldschool.cas;

import org.multiverse.benchmarkframework.executor.AbstractDriver;
import org.multiverse.benchmarkframework.executor.TestCase;
import static org.multiverse.TestUtils.startAll;
import static org.multiverse.TestUtils.joinAll;
import org.multiverse.TestThread;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A Driver for uncontended cas.
 *
 * todo:
 * quality of test is questionable since it could be that the jit removes the cas
 * and replaces it by ordinary reads and writes.
 *
 * @author Peter Veentjer.
 */
public class UncontendedCasDriver extends AbstractDriver {
    private IncrementAndGetThread[] threads;
    private int threadCount;
    private long count;

    @Override
    public void preRun(TestCase testCase) {
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
        private final AtomicLong cas = new AtomicLong();

        public IncrementAndGetThread(int id) {
            super("IncrementAndGetThread-" + id);
        }

        @Test
        public void run(){
             for(long k=0;k<count;k++){
                 cas.incrementAndGet();
             }
        }
    }
}
