package org.multiverse.stms.alpha.instrumentation.asm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.annotations.AtomicMethod;
import org.multiverse.api.annotations.AtomicObject;
import org.multiverse.api.exceptions.ReadonlyException;
import org.multiverse.stms.alpha.AlphaStm;
import org.multiverse.stms.alpha.AlphaStmStatistics;
import org.multiverse.utils.GlobalStmInstance;

/**
 * @author Peter Veentjer
 */
public class AtomicMethod_ReadonlyTransactionTest {

    private AlphaStm stm;
    static private AlphaStmStatistics statistics;

    @Before
    public void setUp() {
        stm = new AlphaStm();
        statistics = stm.getStatistics();
        GlobalStmInstance.set(stm);
    }

    @Test
    public void test() {
        IntRef ref = new IntRef(10);

        long version = stm.getClockVersion();
        long readonlyTransactionStarted = statistics.getReadonlyTransactionStartedCount();
        long readonlyTransactionAborted = statistics.getReadonlyTransactionAbortedCount();
        long readonlyTransactionCommitted = statistics.getReadonlyTransactionCommittedCount();

        int value = ref.get();
        assertEquals(10, value);
        assertEquals(version, stm.getClockVersion());
        assertEquals(readonlyTransactionStarted + 1, statistics.getReadonlyTransactionStartedCount());
        assertEquals(readonlyTransactionAborted, statistics.getReadonlyTransactionAbortedCount());
        assertEquals(readonlyTransactionCommitted + 1, statistics.getReadonlyTransactionCommittedCount());

    }

    @Test
    public void updateIsDetected() {
        IntRef ref = new IntRef(10);

        long version = stm.getClockVersion();
        long readonlyTransactionStarted = statistics.getReadonlyTransactionStartedCount();
        long readonlyTransactionAborted = statistics.getReadonlyTransactionAbortedCount();
        long readonlyTransactionCommitted = statistics.getReadonlyTransactionCommittedCount();

        try {

            ref.readonlySet(11);
            fail();
        } catch (ReadonlyException expected) {
        }

        assertEquals(version, stm.getClockVersion());
        assertEquals(readonlyTransactionStarted + 1, statistics.getReadonlyTransactionStartedCount());
        assertEquals(readonlyTransactionAborted + 1, statistics.getReadonlyTransactionAbortedCount());
        assertEquals(readonlyTransactionCommitted, statistics.getReadonlyTransactionCommittedCount());
        assertEquals(10, ref.get());
    }

    @AtomicObject
    static class IntRef {

        private int value;

        IntRef(int value) {
            this.value = value;
        }

        @AtomicMethod(readonly = true)
        public int get() {
            return value;
        }

        @AtomicMethod(readonly = true)
        public void readonlySet(int value) {
            System.out.println(statistics.getReadonlyTransactionStartedCount());
            this.value = value;
        }

        public void set(int value) {
            this.value = value;
        }
    }
}
