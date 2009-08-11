package org.multiverse.integrationtests;

import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.Stm;
import org.multiverse.api.annotations.AtomicMethod;
import org.multiverse.datastructures.refs.IntRef;
import org.multiverse.utils.GlobalStmInstance;
import static org.multiverse.utils.TransactionThreadLocal.setThreadLocalTransaction;

import java.util.concurrent.atomic.AtomicInteger;


/**
 * This test makes sure that all of the changes are committed, or none of them are committed.
 * <p/>
 * The Test: a shared integer value that is increased by a modification thread. When the transaction begins,
 * it is increased (so the value can't be divided by 2) there will be a delay, another increase (so that
 * the value can be divided by 2) and the transaction commits. Another observing thread that looks at this
 * value should never see a value that can't be divided by 2.
 * <p/>
 * todo:
 * because the isDirty functionality is not implemented, this tests takes longer than needed.
 *
 * @author Peter Veentjer.
 */
public class IsolatedBehaviorLongTest {

    private Stm stm;
    private IntRef intValue;
    private int modifyCount = 300;

    private AtomicInteger modifyCountDown = new AtomicInteger();

    @Before
    public void setUp() {
        stm = GlobalStmInstance.get();
        setThreadLocalTransaction(null);
        intValue = new IntRef(0);
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
                doit();
                sleepRandomMs(1);
            }
        }

        @AtomicMethod
        private void doit() {
            intValue.inc();

            sleepRandomMs(50);

            intValue.inc();
        }
    }

    class ObserveThread extends TestThread {
        public ObserveThread(int threadId) {
            super("ObserveThread-" + threadId);
        }

        @Override
        public void run() {
            while (modifyCountDown.get() > 0) {
                doIt();

                sleepRandomMs(100);
            }
        }

        @AtomicMethod
        private void doIt() {
            if (intValue.get() % 2 != 0) {
                fail();
            }
        }
    }
}
