package org.multiverse.stms.alpha.integrationtests;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Stm;
import org.multiverse.stms.alpha.AlphaTransaction;
import org.multiverse.stms.alpha.manualinstrumentation.IntRef;
import org.multiverse.stms.alpha.manualinstrumentation.IntRefTranlocal;
import org.multiverse.utils.GlobalStmInstance;

/**
 * Tests that a read done by another transaction (readonly or write transaction) doesn't block the
 * write and commit done by another transaction.
 *
 * @author Peter Veentjer.
 */
public class ReadersDontBlockWritersTest {

    private Stm stm;
    private IntRef intValue;

    @Before
    public void setUp() {
        stm = GlobalStmInstance.get();
        intValue = new IntRef(0);
    }

    @Test
    public void testReadWrite() {
        //todo
    }

    @Test
    public void testWriteWrite() {
        AlphaTransaction readTransaction = (AlphaTransaction) stm.startUpdateTransaction();
        IntRefTranlocal r1 = (IntRefTranlocal) readTransaction.privatize(intValue);
        r1.get();

        AlphaTransaction writeTransaction = (AlphaTransaction) stm.startUpdateTransaction();
        IntRefTranlocal writtenValue = (IntRefTranlocal) writeTransaction.privatize(intValue);
        writtenValue.inc();
        writeTransaction.commit();
    }
}
