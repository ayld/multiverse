package org.multiverse.stms.alpha.instrumentation;

import org.junit.Test;
import org.multiverse.api.GlobalStmInstance;
import org.multiverse.stms.alpha.AlphaStm;

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
