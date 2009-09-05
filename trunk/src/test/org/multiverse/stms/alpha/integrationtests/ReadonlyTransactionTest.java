package org.multiverse.stms.alpha.integrationtests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.annotations.AtomicMethod;
import org.multiverse.api.exceptions.ReadonlyException;
import org.multiverse.datastructures.refs.Ref;
import org.multiverse.stms.alpha.AlphaStm;
import org.multiverse.utils.GlobalStmInstance;

public class ReadonlyTransactionTest {
    private AlphaStm stm;

    @Before
    public void setUp() {
        stm = new AlphaStm();
        GlobalStmInstance.set(stm);
    }

    @Test
    public void test() {
        Ref<Integer> ref = new Ref<Integer>(10);

        long version = stm.getClockVersion();

        readonlyMethod(ref, 10);

        assertEquals(version, stm.getClockVersion());
    }

    @AtomicMethod(readonly = true)
    public static void readonlyMethod(Ref<Integer> ref, int expectedValue) {
        ref.set(1);
    }

    @Test
    public void modificationInReadonlyTransactionIsDetected() {
        Ref<Integer> ref = new Ref<Integer>(0);

        long version = stm.getClockVersion();

        try {
            readonlyMethodThatUpdates(ref);
            fail();
        } catch (ReadonlyException ex) {
        }

        assertEquals(version, stm.getClockVersion());
        assertEquals(0, ref);
    }

    @AtomicMethod(readonly = true)
    public static void readonlyMethodThatUpdates(Ref<Integer> ref) {
        ref.set(1);
    }
}
