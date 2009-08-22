package org.multiverse.stms.beta;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import static org.multiverse.TestUtils.joinAll;
import static org.multiverse.TestUtils.startAll;
import org.multiverse.api.Transaction;
import org.multiverse.templates.AtomicTemplate;
import org.multiverse.utils.GlobalStmInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class IncLongTest {

    private int incCount = 2 * 1000 * 1000;
    private int threadCount = 2;
    private int itemCount = 10;
    private BetaStm stm;
    private List<BetaRef<Long>> refs = new ArrayList<BetaRef<Long>>();

    @Before
    public void setUp() {
        stm = new BetaStm();
        GlobalStmInstance.set(stm);
        for (int k = 0; k < itemCount; k++) {
            refs.add(new BetaRef<Long>(0L));
        }
    }

    @Test
    public void test() {
        TestThread[] threads = createThreads();
        long version = stm.getClockVersion();
        long startNs = System.nanoTime();

        startAll(threads);
        joinAll(threads);

        long count = threadCount * incCount;
        assertEquals(version + count, stm.getClockVersion());
        assertEquals(count, sum());

        long periodNs = System.nanoTime() - startNs;
        double transactionPerSecond = (count * 1.0d * TimeUnit.SECONDS.toNanos(1)) / periodNs;
        System.out.printf("%s Transaction/second\n", transactionPerSecond);
    }

    public long sum() {
        long result = 0;
        for (BetaRef<Long> ref : refs) {
            result += ref.get();
        }
        return result;
    }

    public TestThread[] createThreads() {
        TestThread[] threads = new TestThread[threadCount];
        for (int k = 0; k < threadCount; k++) {
            threads[k] = new IncThread(k);
        }
        return threads;
    }

    public class IncThread extends TestThread {
        public IncThread(int id) {
            super("IncThread-" + id);
        }

        @Override
        public void run() {
            for (int k = 0; k < incCount; k++) {
                if ((k % (500 * 1000)) == 0) {
                    System.out.printf("%s is at count %s\n", getName(), k);
                }

                runOnce();
            }
        }

        private void runOnce() {
            new AtomicTemplate() {
                @Override
                public Object execute(Transaction t) throws Exception {
                    BetaTransaction bt = (BetaTransaction) t;
                    int index = (int) (System.nanoTime() % refs.size());
                    BetaRef<Long> ref = refs.get(index);
                    ref.set(bt, ref.get(bt) + 1);
                    return null;
                }
            }.execute();
        }
    }
}
