package org.multiverse.datastructures.collections;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import static org.multiverse.TestUtils.joinAll;
import static org.multiverse.TestUtils.startAll;
import org.multiverse.stms.alpha.AlphaStm;
import org.multiverse.utils.GlobalStmInstance;

public class StrictLinkedBlockingQueueLongTest {
    private int threadCount = 10;
    private AlphaStm stm;
    private StrictLinkedBlockingDeque queue;

    @Before
    public void setUp() {
        stm = new AlphaStm();
        GlobalStmInstance.set(stm);
        queue = new StrictLinkedBlockingDeque();
    }

    @Test
    public void test() {
        StressThread[] threads = createThreads();
        startAll(threads);
        joinAll(threads);
    }

    private StressThread[] createThreads() {
        StressThread[] threads = new StressThread[threadCount];
        for (int k = 0; k < threads.length; k++) {
            threads[k] = new StressThread();
        }
        return threads;
    }

    class StressThread extends TestThread {

        @Override
        public void run() {
        }
    }
}
