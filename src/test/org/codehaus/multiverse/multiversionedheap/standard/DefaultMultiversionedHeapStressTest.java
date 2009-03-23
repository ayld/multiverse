package org.codehaus.multiverse.multiversionedheap.standard;

import org.codehaus.multiverse.TestThread;
import static org.codehaus.multiverse.TestUtils.*;
import org.codehaus.multiverse.multiversionedheap.*;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

import static java.lang.Math.max;
import static java.lang.Math.round;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class DefaultMultiversionedHeapStressTest {

    //the total number of commits we are going to do
    private long commitCount = 200000;
    private int maxCommitSize = 2;
    private int handleRange = 100000;
    private int maxDelaysBetweenTransactions = 0;
    private int maxDelayBetweenStartAndCommit = 0;
    //the number of threads committing and stressing the system
    private int stressThreadCount = 5;

    private DefaultMultiversionedHeap heap;
    private AtomicLong commitCountDown = new AtomicLong();

    @Before
    public void setUp() {
        heap = new DefaultMultiversionedHeap();
    }

    @After
    public void tearDown() {
        System.out.println(heap.getStatistics());
    }

    @Test
    public void test() {
        commitCountDown.set(commitCount);

        TestThread[] threads = createStressThreads(stressThreadCount);

        long startMs = System.currentTimeMillis();
        startAll(threads);
        joinAll(threads);
        long timeMs = (System.currentTimeMillis() - startMs) + 1;

        assertEquals(commitCount, heap.getStatistics().commitSuccessCount.longValue());

        System.out.printf("%s transactions took %s ms\n", commitCount, timeMs);
        System.out.printf("%s transactions/second\n", (commitCount / (timeMs / 1000.0)));
    }

    long randomHandle() {
        return ((System.nanoTime() * 31) % handleRange) + 1;
    }

    int randomUnitOfWorkSize() {
        return max(1, (int) round(Math.random() * maxCommitSize));
    }

    Deflatable[] createUnitOfWork() {
        int unitOfWorkSize = randomUnitOfWorkSize();
        HashMap<Long, Deflatable> set = new HashMap();
        while (set.size() < unitOfWorkSize) {
            long handle = randomHandle();
            if (!set.containsKey(handle))
                set.put(handle, new StringDeflatable(handle));
        }

        Deflatable[] result = new Deflatable[unitOfWorkSize];
        set.values().toArray(result);
        return result;
    }

    StressThread[] createStressThreads(int count) {
        StressThread[] threads = new StressThread[count];
        for (int k = 0; k < count; k++)
            threads[k] = new StressThread();
        return threads;
    }

    private AtomicInteger threadCounter = new AtomicInteger();

    class StressThread extends TestThread {

        public StressThread() {
            super("Thread-" + threadCounter.incrementAndGet());
        }

        public void run() {
            int count = 0;
            while (commitCountDown.getAndDecrement() > 0) {
                runUnitOfWork();

                if (count % 10000 == 0)
                    System.out.println(getName() + " is at commit : " + count);

                sleepRandomMs(maxDelaysBetweenTransactions);
                count++;
            }
        }

        private void runUnitOfWork() {
            Deflatable[] changes = createUnitOfWork();

            sleepRandomMs(maxDelayBetweenStartAndCommit);

            MultiversionedHeap.CommitResult result;
            do {
                HeapSnapshot startSnapshot = heap.getActiveSnapshot();
                result = heap.commit(startSnapshot, changes);
            } while (!result.isSuccess());

            for (Deflatable change : changes) {
                //check that the found content is exactly the same
                Deflated found = result.getSnapshot().read(change.___getHandle());
                assertEquals(change.___deflate(result.getSnapshot().getVersion()), found);
            }
        }
    }
}
