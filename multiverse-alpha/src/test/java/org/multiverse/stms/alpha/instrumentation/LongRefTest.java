package org.multiverse.stms.alpha.instrumentation;

import org.junit.Test;
import static org.multiverse.TestUtils.testIncomplete;
import static org.multiverse.api.GlobalStmInstance.setGlobalStmInstance;
import org.multiverse.stms.alpha.AlphaStm;

public class LongRefTest {
    private AlphaStm stm;

    public void setUp() {
        stm = new AlphaStm();
        setGlobalStmInstance(stm);
    }

    @Test
    public void test() {
        LongRef ref = new LongRef();
        testIncomplete();
    }
}
