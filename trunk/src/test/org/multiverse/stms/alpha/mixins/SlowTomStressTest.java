package org.multiverse.stms.alpha.mixins;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.DummyTransaction;
import org.multiverse.TestThread;
import org.multiverse.api.Stm;
import org.multiverse.api.Transaction;
import org.multiverse.stms.alpha.manualinstrumentation.IntRef;
import org.multiverse.utils.GlobalStmInstance;

/**
 * @author Peter Veentjer
 */
public class SlowTomStressTest {
    private Stm stm;
    private int threadcount = 2;
    private int count = 10 * 1000 * 1000;
    private IntRef intValue = new IntRef(0);

    @Before
    public void setUp() {
        stm = GlobalStmInstance.get();
    }

    @Test
    public void test() {
        StressThread[] threads = createThreads();
        //startAll(threads);
        //joinAll(threads);
    }

    private StressThread[] createThreads() {
        StressThread[] threads = new StressThread[threadcount];
        for (int k = 0; k < threads.length; k++) {
            threads[k] = new StressThread(k);
        }
        return threads;
    }

    public class StressThread extends TestThread {

        public StressThread(int id) {
            super("StressThread-" + id);
        }

        @Override
        public void run() {
            for (int k = 0; k < count; k++) {

                Transaction t = new DummyTransaction();
                long version = stm.getClockVersion();


            }
        }
    }
}
