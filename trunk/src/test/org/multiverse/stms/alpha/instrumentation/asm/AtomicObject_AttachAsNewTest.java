package org.multiverse.stms.alpha.instrumentation.asm;

import org.junit.After;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Transaction;
import org.multiverse.stms.alpha.AlphaAtomicObject;
import org.multiverse.stms.alpha.AlphaStm;
import org.multiverse.stms.alpha.AlphaTransaction;
import org.multiverse.stms.alpha.Tranlocal;
import org.multiverse.utils.GlobalStmInstance;
import static org.multiverse.utils.TransactionThreadLocal.getThreadLocalTransaction;
import org.multiverse.utils.latches.Latch;

/**
 * @author Peter Veentjer
 */
public class AtomicObject_AttachAsNewTest {

    private AlphaStm stm;

    @Before
    public void setUp() {
        stm = new AlphaStm();
        GlobalStmInstance.set(stm);
    }

    @After
    public void tearDown() {
        //assertNoInstrumentationProblems();
    }

    @Test
    public void test() {
        CheckingConstructor foo = new CheckingConstructor();
    }

    public static class CheckingConstructor implements AlphaAtomicObject {
        private int field;

        public CheckingConstructor() {
            AlphaTransaction t = (AlphaTransaction) getThreadLocalTransaction();
            assertNotNull(t);
            Tranlocal tranlocal = t.load(this);
            assertNotNull(tranlocal);
            assertSame(this, tranlocal.getAtomicObject());
        }

        @Override
        public Tranlocal load(long readVersion) {
            throw new RuntimeException();
        }

        @Override
        public Tranlocal privatize(long readVersion) {
            throw new RuntimeException();
        }

        @Override
        public boolean tryLock(Transaction lockOwner) {
            throw new RuntimeException();
        }

        @Override
        public void releaseLock(Transaction expectedLockOwner) {
            throw new RuntimeException();
        }

        @Override
        public void storeAndReleaseLock(Tranlocal tranlocal, long writeVersion) {
            throw new RuntimeException();
        }

        @Override
        public boolean registerRetryListener(Latch listener, long minimumVersion) {
            throw new RuntimeException();
        }

        @Override
        public boolean ensureConflictFree(long readVersion) {
            throw new RuntimeException();
        }

        @Override
        public Transaction getLockOwner() {
            throw new RuntimeException();
        }
    }
}
