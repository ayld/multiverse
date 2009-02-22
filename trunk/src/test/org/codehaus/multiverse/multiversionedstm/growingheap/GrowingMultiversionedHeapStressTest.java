package org.codehaus.multiverse.multiversionedstm.growingheap;

import org.codehaus.multiverse.TestThread;
import static org.codehaus.multiverse.TestUtils.*;
import org.codehaus.multiverse.multiversionedstm.DehydratedStmObject;
import org.codehaus.multiverse.multiversionedstm.DummyDehydratedStmObject;
import org.codehaus.multiverse.multiversionedstm.MultiversionedHeap;
import org.codehaus.multiverse.multiversionedstm.MultiversionedHeapSnapshot;
import org.junit.After;
import static org.junit.Assert.assertSame;
import org.junit.Before;
import org.junit.Test;

import static java.lang.Math.round;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

//todo: a long running test to make sure that there are no memory leaks
public class GrowingMultiversionedHeapStressTest {

    private int commitCount = 200000;
    private int maxCommitSize = 2;
    private int handleRange = 100000;
    private int maxDelaysBetweenTransactions = 0;
    private int maxDelayBetweenStartAndCommit = 0;

    private GrowingMultiversionedHeap heap;
    private AtomicInteger commitCounter = new AtomicInteger();

    @Before
    public void setUp() {
        heap = new GrowingMultiversionedHeap();
    }

    @After
    public void tearDown() {
        System.out.println(heap.getStatistics());
    }

    @Test
    public void test() {
        commitCounter.set(commitCount);
        TestThread[] threads = createStressThreads(2);

        long startMs = System.currentTimeMillis();
        startAll(threads);
        joinAll(threads);
        long timeMs = (System.currentTimeMillis() - startMs) + 1;

        System.out.printf("%s transactions took %s ms\n", commitCount, timeMs);
        System.out.printf("%s transactions/second\n", (commitCount / (timeMs / 1000)));
    }

    long randomHandle() {
        return ((System.nanoTime() * 31) % handleRange) + 1;
    }

    int randomUnitOfWorkSize() {
        return (int) round(Math.random() * maxCommitSize);
    }

    DehydratedStmObject[] createUnitOfWork() {
        int unitOfWorkSize = randomUnitOfWorkSize();
        HashMap<Long, DehydratedStmObject> set = new HashMap();
        while (set.size() < unitOfWorkSize) {
            long handle = randomHandle();
            if (!set.containsKey(handle))
                set.put(handle, new DummyDehydratedStmObject(handle));
        }

        DehydratedStmObject[] result = new DehydratedStmObject[unitOfWorkSize];
        set.values().toArray(result);
        return result;
    }

    public StressThread[] createStressThreads(int count) {
        StressThread[] threads = new StressThread[count];
        for (int k = 0; k < count; k++)
            threads[k] = new StressThread();
        return threads;
    }

    AtomicInteger threadCounter = new AtomicInteger();

    class StressThread extends TestThread {

        public StressThread() {
            super("Thread-" + threadCounter.incrementAndGet());
        }

        public void run() {
            int k = 0;
            while (commitCounter.getAndDecrement() >= 0) {
                runUnitOfWork();

                if (k % 1000 == 0)
                    System.out.println(getName() + " commitcount: " + k);

                sleepRandomMs(maxDelaysBetweenTransactions);
                k++;
            }
        }

        private void runUnitOfWork() {
            DehydratedStmObject[] changes = createUnitOfWork();
            MultiversionedHeapSnapshot startSnapshot = heap.getActiveSnapshot();

            sleepRandomMs(maxDelayBetweenStartAndCommit);

            MultiversionedHeap.CommitResult result = heap.commit(startSnapshot, changes);
            if (result.isSuccess()) {
                MultiversionedHeapSnapshot foundSnapshot = heap.getSnapshot(result.getSnapshot().getVersion());
                assertSame(result.getSnapshot(), foundSnapshot);

                for (DehydratedStmObject change : changes) {
                    //check that the found content is exactly the same
                    DehydratedStmObject found = result.getSnapshot().read(change.getHandle());
                    assertSame(found, change);
                }
            }
        }
    }
}
