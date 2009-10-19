package org.multiverse.stms.alpha;

import static junit.framework.Assert.fail;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.assertIsAborted;
import org.multiverse.api.Transaction;
import org.multiverse.api.TransactionStatus;
import org.multiverse.api.exceptions.NoProgressPossibleException;
import org.multiverse.stms.alpha.manualinstrumentation.IntRef;
import static org.multiverse.utils.TransactionThreadLocal.setThreadLocalTransaction;

public class RegisterRetryListenerTest {
    private AlphaStm stm;

    @Before
    public void setUp() {
        stm = new AlphaStm();
    }

    public Transaction startUpdateTransaction() {
        Transaction t = stm.startUpdateTransaction(null);
        setThreadLocalTransaction(t);
        return t;
    }

    @Test
    public void testNothingRead() {
        Transaction t = startUpdateTransaction();
        long startVersion = stm.getClockVersion();
        try {
            t.abortAndWaitForRetry();
            fail();
        } catch (NoProgressPossibleException ex) {
        }

        assertEquals(startVersion, stm.getClockVersion());
        Assert.assertEquals(TransactionStatus.aborted, t.getStatus());
    }

    @Test
    public void testAttachAsNew() {
        Transaction t = startUpdateTransaction();
        IntRef intValue = new IntRef(0);

        try {
            t.abortAndWaitForRetry();
            fail();
        } catch (NoProgressPossibleException ex) {
        }

        assertIsAborted(t);
    }

    //@Test
    public void test() {
        Transaction t1 = startUpdateTransaction();
        IntRef intValue = new IntRef(10);
        t1.commit();

        Transaction t2 = startUpdateTransaction();
        int result = intValue.get();
        t2.abortAndWaitForRetry();

        assertIsAborted(t2);
    }
}
