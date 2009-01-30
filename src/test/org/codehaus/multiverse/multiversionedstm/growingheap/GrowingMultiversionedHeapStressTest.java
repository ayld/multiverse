package org.codehaus.multiverse.multiversionedstm.growingheap;

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

    private static final int TOTAL_COMMIT_COUNT = 200000;
    private static final int MAX_COMMIT_SIZE = 2;
    private static final int HANDLE_RANGE = 100000;
    private static final int MAX_DELAY_BETWEEN_TRANSACTIONS = 0;
    private static final int MAX_DELAY_BETWEEN_START_AND_COMMIT = 0;

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
        commitCounter.set(TOTAL_COMMIT_COUNT);
        Thread[] threads = createThreads(2);

        long startMs = System.currentTimeMillis();
        startAll(threads);
        joinAll(threads);
        long timeMs = (System.currentTimeMillis() - startMs) + 1;
        System.out.println(String.format("%s transactions took %s ms", TOTAL_COMMIT_COUNT, timeMs));
        System.out.println(String.format("%s transactions/second", (TOTAL_COMMIT_COUNT / (timeMs / 1000))));
    }

    long randomHandle() {
        return ((System.nanoTime() * 31) % HANDLE_RANGE) + 1;
    }

    int randomUnitOfWorkSize() {
        return (int) round(Math.random() * MAX_COMMIT_SIZE);
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

    public Thread[] createThreads(int count) {
        Thread[] threads = new Thread[count];
        for (int k = 0; k < count; k++)
            threads[k] = new TestThread();
        return threads;
    }

    AtomicInteger threadCounter = new AtomicInteger();

    class TestThread extends Thread {

        public TestThread() {
            super("Thread-" + threadCounter.incrementAndGet());
        }

        public void run() {
            int k = 0;
            while (commitCounter.getAndDecrement() >= 0) {
                runUnitOfWork();

                if (k % 1000 == 0)
                    System.out.println(getName() + " commitcount: " + k);

                sleepRandom(MAX_DELAY_BETWEEN_TRANSACTIONS);
                k++;
            }
        }

        private void runUnitOfWork() {
            DehydratedStmObject[] changes = createUnitOfWork();
            MultiversionedHeapSnapshot startSnapshot = heap.getActiveSnapshot();

            sleepRandom(MAX_DELAY_BETWEEN_START_AND_COMMIT);

            MultiversionedHeap.CommitResult result = heap.commit(startSnapshot.getVersion(), changes);
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
