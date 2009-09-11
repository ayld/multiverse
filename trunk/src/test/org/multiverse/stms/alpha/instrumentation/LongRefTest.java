package org.multiverse.stms.alpha.instrumentation;

import org.junit.Test;
import org.multiverse.stms.alpha.AlphaStm;
import org.multiverse.utils.GlobalStmInstance;

public class LongRefTest {
    private AlphaStm stm;

    public void setUp() {
        stm = new AlphaStm();
        GlobalStmInstance.set(stm);
    }

    @Test
    public void test() {
        LongRef ref = new LongRef();
    }
}
