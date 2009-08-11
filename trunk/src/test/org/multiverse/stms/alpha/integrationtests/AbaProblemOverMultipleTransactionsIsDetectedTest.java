package org.multiverse.stms.alpha.integrationtests;

import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Stm;
import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.WriteConflictException;
import org.multiverse.stms.alpha.manualinstrumentation.IntRef;
import org.multiverse.stms.alpha.manualinstrumentation.IntRefTranlocal;
import org.multiverse.utils.GlobalStmInstance;
import static org.multiverse.utils.TransactionThreadLocal.setThreadLocalTransaction;


public class AbaProblemOverMultipleTransactionsIsDetectedTest {

    private static final int A = 1;
    private static final int B = 2;
    private static final int C = 3;

    private Stm stm;
    private IntRef handle;

    @Before
    public void setUp() {
        stm = GlobalStmInstance.get();
        setThreadLocalTransaction(null);
        handle = new IntRef(A);
    }

    public Transaction startUpdateTransaction() {
        Transaction t = stm.startUpdateTransaction();
        setThreadLocalTransaction(t);
        return t;
    }

    @Test
    public void test() {
        Transaction t1 = startUpdateTransaction();
        IntRefTranlocal r1 = (IntRefTranlocal) t1.privatize(handle);

        Transaction t2 = startUpdateTransaction();
        IntRefTranlocal r2 = (IntRefTranlocal) t2.privatize(handle);
        r2.set(B);
        t2.commit();

        Transaction t3 = startUpdateTransaction();
        IntRefTranlocal r3 = (IntRefTranlocal) t3.privatize(handle);
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
