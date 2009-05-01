package org.multiverse.tests;

import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.Originator;
import org.multiverse.api.Transaction;
import org.multiverse.api.TransactionTemplate;
import org.multiverse.api.exceptions.AbortedTransactionException;
import org.multiverse.multiversionedstm.MultiversionedStm;
import org.multiverse.multiversionedstm.examples.IntegerValue;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A test that checks if modifications are done atomically. So a transactions that are aborted, should
 * not be committed (not even partially) to the heap.
 * <p/>
 * The test: there is a modification thread that updates an integervalue. The only valid value that is permitted
 * in the heap is a value that can be divided by 2. The update is done in 2 staps that increase the value by one
 * and in some cases the transaction is aborted.
 *
 * @author Peter Veentjer.
 */
public class AtomicBehaviorTest {

    private MultiversionedStm stm;

    private Originator<IntegerValue> originator;
    private int modifyCount = 500;
    private AtomicInteger modifyCountDown = new AtomicInteger();

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
        originator = commit(stm, new IntegerValue());
    }

    @Test
    public void test() {
        modifyCountDown.set(modifyCount);

        ModifyThread modifyThread = new ModifyThread(0);
        ObserverThread observerThread = new ObserverThread();

        startAll(modifyThread, observerThread);
        joinAll(modifyThread, observerThread);
    }

    class ModifyThread extends TestThread {
        public ModifyThread(int id) {
            super("ModifyThread-" + id);
        }

        public void run() {
            while (modifyCountDown.getAndDecrement() > 0) {
                try {
                    doit();
                } catch (AbortedTransactionException ex) {
                }
            }
        }

        public void doit() {
            new TransactionTemplate(stm) {
                @Override
                protected Object execute(Transaction t) throws Exception {
                    IntegerValue value = (IntegerValue) t.read(originator);
                    if (value.get() % 2 != 0)
                        fail();

                    value.inc();

                    sleepRandomMs(20);

                    if (randomBoolean()) {
                        t.abort();
                        return null;
                    }

                    value.inc();
                    return null;
                }
            }.execute();
        }
    }

    class ObserverThread extends TestThread {
        public ObserverThread() {
            super("ObserverThread");
        }

        @Override
        public void run() {
            while (modifyCountDown.get() > 0) {
                new TransactionTemplate(stm) {
                    @Override
                    protected Object execute(Transaction t) throws Exception {
                        IntegerValue value = (IntegerValue) t.read(originator);
                        if (value.get() % 2 != 0)
                            fail();

                        return null;
                    }
                }.execute();

                sleepRandomMs(5);
            }
        }
    }
}
