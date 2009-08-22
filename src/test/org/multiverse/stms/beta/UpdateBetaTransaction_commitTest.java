package org.multiverse.stms.beta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.assertIsAborted;
import static org.multiverse.TestUtils.assertIsCommitted;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.utils.GlobalStmInstance;

public class UpdateBetaTransaction_commitTest {
    private BetaStm stm;

    @Before
    public void setUp() {
        stm = new BetaStm();
        GlobalStmInstance.set(stm);
    }

    private BetaTransaction startTransaction() {
        return (BetaTransaction) stm.startUpdateTransaction();
    }

    @Test
    public void insert() {
        long version = stm.getClockVersion();

        BetaTransaction t = startTransaction();
        BetaRef ref = new BetaRef(t);
        t.commit();

        assertIsCommitted(t);
        assertEquals(version + 1, stm.getClockVersion());
        //todo: rev value controleren
    }

    @Test
    public void update() {
        BetaRef<String> ref = new BetaRef<String>("foo");

        long version = stm.getClockVersion();

        BetaTransaction t = startTransaction();
        ref.set(t, "bar");
        t.commit();

        assertIsCommitted(t);
        assertEquals(version + 1, stm.getClockVersion());
        assertEquals("bar", ref.get());
    }

    @Test
    public void noChanges() {
        String value = "foo";
        BetaRef<String> ref = new BetaRef<String>(value);

        long version = stm.getClockVersion();

        BetaTransaction t = startTransaction();
        ref.set(value);
        t.commit();

        assertIsCommitted(t);
        assertEquals(version, stm.getClockVersion());
        assertEquals(value, ref.get());
    }

    @Test
    public void writeConflict() {
        //todo
    }

    @Test
    public void lockFailure() {
        //todo
    }

    @Test
    public void multipleInserts() {
        long version = stm.getClockVersion();

        BetaTransaction t = startTransaction();
        BetaRef<String> ref1 = new BetaRef<String>(t, "1");
        BetaRef<String> ref2 = new BetaRef<String>(t, "2");
        BetaRef<String> ref3 = new BetaRef<String>(t, "3");
        t.commit();

        assertEquals(version + 1, stm.getClockVersion());
        assertIsCommitted(t);
        assertEquals("1", ref1.get());
        assertEquals("2", ref2.get());
        assertEquals("3", ref3.get());
    }

    @Test
    public void multipleUpdates() {
        BetaRef<String> ref1 = new BetaRef<String>("1");
        BetaRef<String> ref2 = new BetaRef<String>("2");
        BetaRef<String> ref3 = new BetaRef<String>("3");

        long version = stm.getClockVersion();

        BetaTransaction t = startTransaction();
        ref1.set(t, "a");
        ref2.set(t, "b");
        ref3.set(t, "c");
        t.commit();

        assertEquals(version + 1, stm.getClockVersion());
        assertIsCommitted(t);
        assertEquals("a", ref1.get());
        assertEquals("b", ref2.get());
        assertEquals("c", ref3.get());
    }

    @Test
    public void commitUnusedTransaction() {
        BetaTransaction t = startTransaction();
        long version = stm.getClockVersion();

        t.commit();

        assertEquals(version, stm.getClockVersion());
        assertIsCommitted(t);
    }

    @Test
    public void commitOnAbortedTransactionFails() {
        BetaTransaction t = startTransaction();
        t.abort();

        long version = stm.getClockVersion();
        try {
            t.commit();
            fail();
        } catch (DeadTransactionException ex) {
        }

        assertEquals(version, stm.getClockVersion());
        assertIsAborted(t);
    }

    @Test
    public void commitOnCommittedTransactionIsIgnored() {
        BetaTransaction t = startTransaction();
        t.commit();

        long version = stm.getClockVersion();
        t.commit();

        assertEquals(version, stm.getClockVersion());
        assertIsCommitted(t);
    }
}
