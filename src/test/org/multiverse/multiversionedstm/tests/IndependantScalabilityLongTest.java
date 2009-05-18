package org.multiverse.multiversionedstm.tests;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.Handle;
import org.multiverse.api.Transaction;
import org.multiverse.api.TransactionTemplate;
import org.multiverse.multiversionedstm.MultiversionedStm;
import org.multiverse.multiversionedstm.examples.ExampleIntegerValue;

public class IndependantScalabilityLongTest {
    private MultiversionedStm stm;
    private long updateCount = 10000000;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
    }

    @After
    public void tearDown() {
        System.out.println(stm.getStatistics());
    }

    @Test
    public void test() {
        for (int k = 1; k <= Runtime.getRuntime().availableProcessors(); k++) {
            test(k);
        }
    }

    public void test(int threadCount) {
        System.out.printf("starting with %s threads\n", threadCount);

        long startMs = System.currentTimeMillis();
        TestThread[] threads = createThreads(threadCount);
        startAll(threads);
        joinAll(threads);
        long endMs = System.currentTimeMillis();
        System.out.printf("execution took %s ms\n", (endMs - startMs));
    }

    public MyThread[] createThreads(int threadCount) {
        MyThread[] threads = new MyThread[threadCount];
        for (int k = 0; k < threads.length; k++) {
            threads[k] = new MyThread(k, commit(stm, new ExampleIntegerValue()));
        }
        return threads;
    }

    class MyThread extends TestThread {
        private Handle<ExampleIntegerValue> handle;

        public MyThread(int id, Handle<ExampleIntegerValue> handle) {
            super("Thread-" + id);
            this.handle = handle;
        }

        @Override
        public void run() {
            for (int k = 0; k < updateCount; k++) {
                new TransactionTemplate(stm) {
                    @Override
                    protected Object execute(Transaction t) throws Exception {
                        ExampleIntegerValue value = t.read(handle);
                        value.inc();
                        return null;
                    }
                }.execute();
            }
        }
    }
}
