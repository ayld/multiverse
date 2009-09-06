package org.multiverse.stms.alpha.integrationtests;

import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.exceptions.WriteConflictException;
import org.multiverse.stms.alpha.AlphaStm;
import org.multiverse.stms.alpha.AlphaTransaction;
import org.multiverse.stms.alpha.manualinstrumentation.IntRef;
import org.multiverse.stms.alpha.manualinstrumentation.IntRefTranlocal;
import org.multiverse.utils.GlobalStmInstance;
import static org.multiverse.utils.TransactionThreadLocal.setThreadLocalTransaction;


public class AbaProblemOverMultipleTransactionsIsDetectedTest {

    private static final int A = 1;
    private static final int B = 2;
    private static final int C = 3;

    private AlphaStm stm;
    private IntRef handle;

    @Before
    public void setUp() {
        stm = new AlphaStm();
        GlobalStmInstance.set(stm);
        setThreadLocalTransaction(null);
        handle = new IntRef(A);
    }

    public AlphaTransaction startUpdateTransaction() {
        AlphaTransaction t = stm.startUpdateTransaction(null);
        setThreadLocalTransaction(t);
        return t;
    }

    @Test
    public void test() {
        AlphaTransaction t1 = startUpdateTransaction();
        IntRefTranlocal r1 = (IntRefTranlocal) t1.load(handle);

        AlphaTransaction t2 = startUpdateTransaction();
        IntRefTranlocal r2 = (IntRefTranlocal) t2.load(handle);
        r2.set(B);
        t2.commit();

        AlphaTransaction t3 = startUpdateTransaction();
        IntRefTranlocal r3 = (IntRefTranlocal) t3.load(handle);
        r3.set(B);
        t3.commit();

        r1.set(C);
        try {
            t1.commit();
            fail();
        } catch (WriteConflictException er) {
        }
    }
}
