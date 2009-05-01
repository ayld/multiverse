package org.multiverse.multiversionedstm.tests;

import org.junit.After;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.Handle;
import org.multiverse.api.Transaction;
import org.multiverse.api.TransactionTemplate;
import org.multiverse.multiversionedstm.MultiversionedStm;
import org.multiverse.multiversionedstm.PrintMultiversionedStmStatisticsThread;
import org.multiverse.multiversionedstm.examples.IntegerValue;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * This test makes sure that all of the changes are committed, or none of them are committed.
 * <p/>
 * The Test: a shared integer value that is increased by a modification thread. When the transaction begins,
 * it is increased (so the value can't be divided by 2) there will be a delay, another increase (so that
 * the value can be divided by 2) and the transaction commits. Another observing thread that looks at this
 * value should never see a value that can't be divided by 2.
 *
 * @author Peter Veentjer.
 */
public class IsolatedBehaviorTest {

    private MultiversionedStm stm;
    private Handle<IntegerValue> handle;
    private int modifyCount = 300;

    private AtomicInteger modifyCountDown = new AtomicInteger();

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
        handle = commit(stm, new IntegerValue());
        new PrintMultiversionedStmStatisticsThread(stm).start();
    }

    @After
    public void tearDown() {
        System.out.println(stm.getStatistics());
    }

    @Test
    public void test() {
        modifyCountDown.set(modifyCount);

        ModifyThread modifyThread = new ModifyThread(1);
        ObserveThread observeThread = new ObserveThread(1);

        startAll(modifyThread, observeThread);
        joinAll(modifyThread, observeThread);
    }

    class ModifyThread extends TestThread {
        public ModifyThread(int threadId) {
            super("ModifyThread-" + threadId);
        }

        @Override
        public void run() {
            while (modifyCountDown.decrementAndGet() > 0) {
                new TransactionTemplate(stm) {
                    @Override
                    protected Object execute(Transaction t) throws Exception {
                        IntegerValue value = (IntegerValue) t.read(handle);
                        value.inc();

                        sleepRandomMs(50);

                        value.inc();
                        return null;
                    }
                }.execute();

                sleepRandomMs(1);
            }
        }
    }

    class ObserveThread extends TestThread {
        public ObserveThread(int threadId) {
            super("ObserveThread-" + threadId);
        }

        @Override
        public void run() {
            while (modifyCountDown.get() > 0) {
                new TransactionTemplate(stm) {
                    @Override
                    protected Object execute(Transaction t) throws Exception {
                        IntegerValue value = (IntegerValue) t.read(handle);
                        if (value.get() % 2 != 0)
                            fail();

                        return null;
                    }
                }.execute();

                sleepRandomMs(1);
            }
        }
    }
}
