package org.multiverse.stms.beta;

import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.exceptions.ReadonlyException;
import org.multiverse.utils.GlobalStmInstance;

public class ReadonlyBetaTransaction_privatizeTest {

    private BetaStm stm;

    @Before
    public void setUp() {
        stm = new BetaStm();
        GlobalStmInstance.set(stm);
    }

    private BetaTransaction startTransaction() {
        return stm.startReadOnlyTransaction(null);
    }

    @Test
    public void test() {
        BetaTransaction t = startTransaction();
        BetaRef ref = new BetaRef();

        try {
            t.privatize(ref);
            fail();
        } catch (ReadonlyException ex) {
        }
    }
}
