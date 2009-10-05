package org.multiverse.integrationtests;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.stms.alpha.AlphaStm;
import org.multiverse.utils.GlobalStmInstance;
import static org.multiverse.utils.TransactionThreadLocal.setThreadLocalTransaction;

/**
 * @author Peter Veentjer
 */
public class IsDirtyLongTest {
    private AlphaStm stm;

    @Before
    public void setUp() {
        stm = new AlphaStm();
        GlobalStmInstance.set(stm);
        setThreadLocalTransaction(null);
    }

    @After
    public void tearDown() {
        //    stm.getProfiler().print();
    }

    @Test
    public void test() {

    }
}
