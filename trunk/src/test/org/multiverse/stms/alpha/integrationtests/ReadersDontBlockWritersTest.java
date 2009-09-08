package org.multiverse.stms.alpha.integrationtests;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.stms.alpha.AlphaStm;
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

    private AlphaStm stm;
    private IntRef ref;

    @Before
    public void setUp() {
        stm = new AlphaStm();
        GlobalStmInstance.set(stm);
        ref = new IntRef(0);
    }

    @Test
    public void testReadWrite() {
        //todo
    }

    @Test
    public void testWriteWrite() {
        AlphaTransaction readTransaction = stm.startUpdateTransaction(null);
        IntRefTranlocal r1 = (IntRefTranlocal) readTransaction.load(ref);
        ref.get(r1);

        AlphaTransaction writeTransaction = stm.startUpdateTransaction(null);
        IntRefTranlocal writtenValue = (IntRefTranlocal) writeTransaction.load(ref);
        ref.inc(writtenValue);
        writeTransaction.commit();
    }
}
