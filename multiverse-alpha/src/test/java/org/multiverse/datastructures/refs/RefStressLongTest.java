package org.multiverse.datastructures.refs;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.GlobalStmInstance.setGlobalStmInstance;
import org.multiverse.api.annotations.AtomicMethod;
import org.multiverse.stms.alpha.AlphaStm;

import java.util.concurrent.atomic.AtomicInteger;

public class RefStressLongTest {

    private int threadCount = 5;
    private int transactionCount = 2 * 1000 * 1000;
    private int refCount = 20;
    private IntRef[] refs;
    private AtomicInteger total = new AtomicInteger();
    private StressThread[] threads;

    @Before
    public void setUp() {
        setGlobalStmInstance(AlphaStm.createFast());
        total.set(0);
        threads = new StressThread[threadCount];
        for (int k = 0; k < threads.length; k++) {
            threads[k] = new StressThread(k);
        }
        refs = new IntRef[refCount];
        for (int k = 0; k < refs.length; k++) {
            refs[k] = new IntRef(0);
        }
    }

    @Test
    public void test() {
        startAll(threads);
        joinAll(threads);

        assertEquals(total.get(), sum());
    }

    public int sum() {
        int result = 0;
        for (IntRef intRef : refs) {
            result += intRef.get();
        }
        return result;
    }

    public class StressThread extends TestThread {

        public StressThread(int id) {
            super("StressThread-" + id);
        }

        @Override
        public void doRun() throws Exception {
            for (int k = 0; k < transactionCount; k++) {
                int count = doIt();
                total.addAndGet(count);

                if (k % 100000 == 0) {
                    System.out.printf("%s is at %s\n", getName(), k);
                }
            }
        }

        @AtomicMethod
        private int doIt() {
            int count = 0;
            for (IntRef ref : refs) {
                if (randomInt(10) % 5 == 2) {
                    count++;
                    ref.inc();
                }
            }
            return count;
        }
    }
}
