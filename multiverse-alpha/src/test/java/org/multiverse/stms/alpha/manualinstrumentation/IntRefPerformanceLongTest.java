package org.multiverse.stms.alpha.manualinstrumentation;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.api.GlobalStmInstance.setGlobalStmInstance;
import org.multiverse.stms.alpha.AlphaStm;
import static org.multiverse.stms.alpha.AlphaStmConfig.createFastConfig;
import static org.multiverse.utils.ThreadLocalTransaction.setThreadLocalTransaction;

import java.util.concurrent.TimeUnit;

public class IntRefPerformanceLongTest {
    private int count = 3 * 1000 * 1000;

    private AlphaStm stm;

    @Before
    public void setUp() {
        stm = new AlphaStm(createFastConfig());
        setGlobalStmInstance(stm);
        setThreadLocalTransaction(null);
    }

    @After
    public void tearDown() {
        setThreadLocalTransaction(null);
        if (stm.getProfiler() != null) {
            //stm.getProfiler().print();
        }
    }

    @Test
    public void test() {
        IntRef value = new IntRef(10);

        long startNs = System.nanoTime();

        for (int k = 0; k < count; k++) {
            value.inc();
        }

        long periodNs = System.nanoTime() - startNs;
        double transactionPerSecond = (count * 1.0d * TimeUnit.SECONDS.toNanos(1)) / periodNs;
        System.out.printf("%s Transaction/second\n", transactionPerSecond);
    }
}
