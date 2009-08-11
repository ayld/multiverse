package org.multiverse.stms.alpha.instrumentation;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.annotations.AtomicMethod;
import org.multiverse.stms.alpha.manualinstrumentation.IntRef;
import org.multiverse.stms.alpha.manualinstrumentation.LongRef;
import org.multiverse.stms.alpha.AlphaStm;
import org.multiverse.utils.GlobalStmInstance;
import static org.multiverse.utils.TransactionThreadLocal.setThreadLocalTransaction;

/**
 * @author Peter Veentjer
 */
public class AtomicClassTransformerIntegrationTest {
    private AlphaStm stm;

    @Before
    public void setUp() {
        stm = new AlphaStm();
        GlobalStmInstance.set(stm);
        setThreadLocalTransaction(null);
    }

    @Test
    public void testInstanceMethod() {
        IntRef v1 = new IntRef(0);

        inc(v1);

        assertEquals(1, v1.get());
    }

    @AtomicMethod
    public void inc(IntRef v1) {
        v1.inc();
    }

    @Test
    public void testStaticMethod() {
        IntRef v1 = new IntRef(0);

        incStatic(v1);

        assertEquals(1, v1.get());
    }

    @AtomicMethod
    public static void incStatic(IntRef v1) {
        v1.inc();
    }

    @Test
    public void testMultipleUpdates() {
        LongRef v1 = new LongRef(0);
        LongRef v2 = new LongRef(1);
        LongRef v3 = new LongRef(2);

        long clockVersion = stm.getClockVersion();

        updateToStmClockVersion(v1, v2, v3);

        assertEquals(clockVersion, v1.get());
        assertEquals(clockVersion, v2.get());
        assertEquals(clockVersion, v3.get());
    }

    @AtomicMethod
    public void updateToStmClockVersion(LongRef... values) {
        for (LongRef value : values) {
            value.set(stm.getClockVersion());
        }
    }
}
