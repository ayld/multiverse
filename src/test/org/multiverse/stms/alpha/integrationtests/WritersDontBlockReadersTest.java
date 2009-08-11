package org.multiverse.stms.alpha.integrationtests;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Stm;
import org.multiverse.api.Transaction;
import org.multiverse.stms.alpha.manualinstrumentation.IntRef;
import org.multiverse.stms.alpha.manualinstrumentation.IntRefTranlocal;
import org.multiverse.utils.GlobalStmInstance;
import static org.multiverse.utils.TransactionThreadLocal.setThreadLocalTransaction;

public class WritersDontBlockReadersTest {
    private Stm stm;
    private IntRef value;

    @Before
    public void setUp() {
        stm = GlobalStmInstance.get();
        setThreadLocalTransaction(null);
        value = new IntRef(0);
    }

    @Test
    public void testOneTransactionIsAReadonlyTransaction() {
        //todo
    }

    @Test
    public void testBothTransactionsAreWriteTransactions() {
        Transaction writeTransaction = stm.startUpdateTransaction();
        IntRefTranlocal writtenValue = (IntRefTranlocal) writeTransaction.privatize(value);
        writtenValue.inc();

        Transaction readTransaction = stm.startUpdateTransaction();
        IntRefTranlocal readValue = (IntRefTranlocal) readTransaction.privatize(value);
        int value = readValue.get();
        readTransaction.commit();

        assertEquals(0, value);
    }
}
