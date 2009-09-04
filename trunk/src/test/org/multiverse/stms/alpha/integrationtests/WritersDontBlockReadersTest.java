package org.multiverse.stms.alpha.integrationtests;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.stms.alpha.AlphaStm;
import org.multiverse.stms.alpha.AlphaTransaction;
import org.multiverse.stms.alpha.manualinstrumentation.IntRef;
import org.multiverse.stms.alpha.manualinstrumentation.IntRefTranlocal;
import org.multiverse.utils.GlobalStmInstance;
import static org.multiverse.utils.TransactionThreadLocal.setThreadLocalTransaction;

public class WritersDontBlockReadersTest {
    private AlphaStm stm;
    private IntRef value;

    @Before
    public void setUp() {
        stm = new AlphaStm();
        GlobalStmInstance.set(stm);
        setThreadLocalTransaction(null);
        value = new IntRef(0);
    }

    @Test
    public void testOneTransactionIsAReadonlyTransaction() {
        //todo
    }

    @Test
    public void testBothTransactionsAreWriteTransactions() {
        AlphaTransaction writeTransaction = stm.startUpdateTransaction(null);
        IntRefTranlocal writtenValue = (IntRefTranlocal) writeTransaction.load(value);
        writtenValue.inc();

        AlphaTransaction readTransaction = stm.startUpdateTransaction(null);
        IntRefTranlocal readValue = (IntRefTranlocal) readTransaction.load(value);
        int value = readValue.get();
        readTransaction.commit();

        assertEquals(0, value);
    }
}
