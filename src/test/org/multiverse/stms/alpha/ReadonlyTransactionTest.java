package org.multiverse.stms.alpha;

import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.*;
import org.multiverse.DummyTranlocal;
import org.multiverse.stms.alpha.AlphaStm;
import org.multiverse.api.Tranlocal;
import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.*;
import org.multiverse.stms.alpha.manualinstrumentation.IntRef;
import org.multiverse.stms.alpha.manualinstrumentation.IntRefTranlocal;
import org.multiverse.utils.GlobalStmInstance;
import static org.multiverse.utils.TransactionThreadLocal.setThreadLocalTransaction;

public class ReadonlyTransactionTest {
    private AlphaStm stm;

    @Before
    public void setUp() {
        stm = new AlphaStm();
        GlobalStmInstance.set(stm);
        setThreadLocalTransaction(null);
    }

    @After
    public void after() {
        setThreadLocalTransaction(null);
    }

    public Transaction startUpdateTransaction() {
        Transaction t = stm.startUpdateTransaction();
        setThreadLocalTransaction(t);
        return t;
    }

    public Transaction startReadonlyTransaction() {
        Transaction t = stm.startReadOnlyTransaction();
        setThreadLocalTransaction(t);
        return t;
    }

    // ====================== reset ==========================================

    public void resetOnActiveTransactionFails() {
        //todo
    }

    public void resetOnAbortedTransaction() {
        //todo
    }

    public void resetOnCommittedTransaction() {
        //todo
    }

    //====================== loadReadonly ====================================

    @Test
    public void start() {
        long version = stm.getClockVersion();
        Transaction t = stm.startReadOnlyTransaction();
        assertIsActive(t);
        assertEquals(version, stm.getClockVersion());
        assertEquals(1, stm.getStatistics().getReadonlyTransactionStartedCount());
    }

    @Test
    public void readNullReturnsNull() {
        Transaction t = startReadonlyTransaction();
        Tranlocal result = t.load(null);
        assertNull(result);
    }

    @Test
    public void readNonCommitted() {
        IntRef value = IntRef.createUncommitted();

        Transaction t = startReadonlyTransaction();

        try {
            t.load(value);
            fail();
        } catch (LoadUncommittedAtomicObjectException ex) {
        }

        assertIsActive(t);
    }

    @Test
    public void readPreviouslyCommitted() {
        IntRef value = new IntRef(10);

        IntRefTranlocal expected = (IntRefTranlocal) value.load(stm.getClockVersion());

        Transaction t2 = startReadonlyTransaction();
        IntRefTranlocal found = (IntRefTranlocal) t2.load(value);
        assertSame(expected, found);
    }

    @Test
    public void readDoesNotObserveChangesMadeByOtherTransactions() {
        IntRef value = new IntRef(0);

        Transaction readonlyTransaction = stm.startReadOnlyTransaction();
        Transaction updateTransaction = stm.startUpdateTransaction();
        IntRefTranlocal tranlocal = (IntRefTranlocal) updateTransaction.privatize(value);
        tranlocal.inc();

        IntRefTranlocal tranlocalIntValue = (IntRefTranlocal) readonlyTransaction.load(value);
        assertEquals(0, tranlocalIntValue.get());
    }

    @Test
    public void readOnCommittedTransactionFails() {
        IntRef value = new IntRef(10);

        Transaction t = startReadonlyTransaction();
        t.commit();

        try {
            t.load(value);
            fail();
        } catch (DeadTransactionException ex) {
        }

        assertIsCommitted(t);
    }


    @Test
    public void readOnAbortedTransactionFails() {
        IntRef value = new IntRef(10);

        Transaction t = startReadonlyTransaction();
        t.abort();

        try {
            t.load(value);
            fail();
        } catch (DeadTransactionException ex) {
        }

        assertIsAborted(t);
    }


    //====================== readPrivatized =====================

    @Test
    public void readPrivatizedOnStartedTransactionFails() {
        Transaction t = startReadonlyTransaction();

        try {
            t.privatize(new IntRef(1));
            fail();
        } catch (ReadonlyException ex) {
        }

        assertIsActive(t);
    }

    @Test
    public void readPrivatizedOnCommittedTransactionFails() {
        Transaction t = startReadonlyTransaction();
        t.commit();

        try {
            t.privatize(new IntRef(1));
            fail();
        } catch (ReadonlyException ex) {
        }

        assertIsCommitted(t);
    }

    @Test
    public void readPrivatizedOnAbortedTransactionFails() {
        Transaction t = startReadonlyTransaction();
        t.abort();

        try {
            t.privatize(new IntRef(1));
            fail();
        } catch (ReadonlyException ex) {
        }

        assertIsAborted(t);
    }

    //========================== attachAsNew ===================

    @Test
    public void attachAsNewOnStartedTransactionShouldFail() {
        Transaction t = startReadonlyTransaction();

        try {
            t.attachNew(new DummyTranlocal());
            fail();
        } catch (ReadonlyException ex) {
        }

        assertIsActive(t);
    }

    @Test
    public void attachAsNewOnCommittedTransactionShouldFail() {
        Transaction t = startReadonlyTransaction();
        t.commit();

        try {
            t.attachNew(new DummyTranlocal());
            fail();
        } catch (ReadonlyException ex) {
        }

        assertIsCommitted(t);
    }

    @Test
    public void attachAsNewOnAbortedTransactionShouldFail() {
        Transaction t = startReadonlyTransaction();
        t.abort();

        try {
            t.attachNew(new DummyTranlocal());
            fail();
        } catch (ReadonlyException ex) {
        }

        assertIsAborted(t);
    }

    // ========================= abort =========================

    @Test
    public void abortStartedTransaction() {
        long startVersion = stm.getClockVersion();
        Transaction t = startReadonlyTransaction();
        t.abort();

        assertEquals(startVersion, stm.getClockVersion());
        assertIsAborted(t);
    }

    @Test
    public void abortCommittedTransactionShouldFail() {
        Transaction t = startReadonlyTransaction();
        t.commit();

        long startVersion = stm.getClockVersion();
        try {
            t.abort();
            fail();
        } catch (DeadTransactionException ex) {
        }

        assertEquals(startVersion, stm.getClockVersion());
        assertIsCommitted(t);
    }

    public void abortAbortedTransactionIsIgnored() {
        Transaction t = startReadonlyTransaction();
        t.abort();

        long startVersion = stm.getClockVersion();
        t.abort();
        assertEquals(startVersion, stm.getClockVersion());
        assertIsAborted(t);
    }

    // ====================== abort and retry ==================

    @Test
    public void abortAndRetryFailsIfTransactionIsStarted() {
        Transaction t = startReadonlyTransaction();

        try {
            t.abortAndRetry();
            fail();
        } catch (ReadonlyException ex) {
        }

        assertIsActive(t);
    }

    @Test
    public void abortAndRetryFailsIfTransactionIsCommitted() {
        Transaction t = startReadonlyTransaction();
        t.commit();

        try {
            t.abortAndRetry();
            fail();
        } catch (ReadonlyException ex) {
        }

        assertIsCommitted(t);
    }

    @Test
    public void abortAndRetryFailsIfTransactionIsAborted() {
        Transaction t = startReadonlyTransaction();
        t.abort();

        try {
            t.abortAndRetry();
            fail();
        } catch (ReadonlyException ex) {
        }

        assertIsAborted(t);
    }

    // ======================== commit =========================

    @Test
    public void commitStartedTransaction() {
        long startVersion = stm.getClockVersion();
        Transaction t = startReadonlyTransaction();
        t.commit();

        assertEquals(startVersion, stm.getClockVersion());
        assertIsCommitted(t);
    }

    @Test
    public void commitIsIgnoredIfAlreadyCommitted() {
        Transaction t = startReadonlyTransaction();
        t.commit();

        long startVersion = stm.getClockVersion();
        t.commit();

        assertEquals(startVersion, stm.getClockVersion());
        assertIsCommitted(t);
    }

    @Test
    public void commitFailsIfAlreadyAborted() {
        Transaction t = startReadonlyTransaction();
        t.abort();

        long startVersion = stm.getClockVersion();
        try {
            t.commit();
            fail();
        } catch (DeadTransactionException ex) {
        }

        assertIsAborted(t);
        assertEquals(startVersion, stm.getClockVersion());
    }
}
