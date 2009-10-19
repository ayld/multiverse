package org.multiverse.stms.alpha.instrumentation;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.GlobalStmInstance;
import org.multiverse.stms.alpha.AlphaStm;

/**
 * @author Peter Veentjer
 */
public class StaticInnerClassThatIsNotAnAtomicObjectTest {

    private AlphaStm stm;

    @Before
    public void setUp() {
        stm = new AlphaStm();
        GlobalStmInstance.set(stm);
    }

    @Test
    public void test() {

    }
}
