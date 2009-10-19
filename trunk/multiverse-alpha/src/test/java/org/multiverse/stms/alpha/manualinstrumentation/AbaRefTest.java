package org.multiverse.stms.alpha.manualinstrumentation;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.GlobalStmInstance;
import org.multiverse.api.Transaction;
import org.multiverse.stms.alpha.AlphaStm;
import static org.multiverse.utils.TransactionThreadLocal.setThreadLocalTransaction;

/**
 * @author Peter Veentjer
 */
public class AbaRefTest {
    private AlphaStm stm;

    @Before
    public void setUp() {
        stm = new AlphaStm();
        setThreadLocalTransaction(null);
        GlobalStmInstance.set(stm);
    }

    public Transaction startTransaction() {
        Transaction t = stm.startUpdateTransaction(null);
        setThreadLocalTransaction(t);
        return t;
    }

    @Test
    public void testAbaProblemIsDetected() {
        final String a = "A";
        final String b = "B";

        final AbaRef<String> ref = new AbaRef<String>(a);

        long startVersion = stm.getClockVersion();
        Transaction t = startTransaction();
        ref.set(b);
        ref.set(a);
        t.commit();
        assertEquals(startVersion + 1, stm.getClockVersion());
    }

}
