package org.multiverse.stms.beta;

import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.assertIsActive;
import org.multiverse.api.exceptions.ReadonlyException;
import org.multiverse.utils.GlobalStmInstance;

public class ReadonlyBetaTransaction_attachNewTest {
    private BetaStm stm;

    @Before
    public void setUp() {
        stm = new BetaStm();
        GlobalStmInstance.set(stm);
    }

    private BetaTransaction startTransaction() {
        return (BetaTransaction) stm.startReadOnlyTransaction();
    }

    @Test
    public void test() {
        BetaTransaction t = startTransaction();
        BetaRef ref = new BetaRef();

        try {
            t.attachNew(new BetaRefTranlocal(ref));
            fail();
        } catch (ReadonlyException ex) {
        }

        assertIsActive(t);
    }
}
