package org.multiverse.stms.alpha.instrumentation.asm;

import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.testIncomplete;
import org.multiverse.api.GlobalStmInstance;
import org.multiverse.api.Stm;
import org.multiverse.api.annotations.AtomicObject;
import org.multiverse.stms.alpha.AlphaStm;
import static org.multiverse.utils.TransactionThreadLocal.setThreadLocalTransaction;

public class AtomicObject_clashingFieldTest {

    private Stm stm;

    @Before
    public void setUp() {
        stm = new AlphaStm();
        GlobalStmInstance.set(stm);
        setThreadLocalTransaction(null);
    }

    @Test
    public void test() throws NoSuchFieldException {
        testIncomplete();

        /*
        long version = stm.getClockVersion();

        ObjectWithClashingField o = new ObjectWithClashingField(10);

        assertEquals(version + 1, stm.getClockVersion());
        assertTrue(((Object) o) instanceof AlphaAtomicObject);

        ClassUtils.printDescription(o.getClass());

        Field field = o.getClass().getField("lockOwner");
        assertEquals(Integer.TYPE, field.getType());*/
    }

    @AtomicObject
    static class ObjectWithClashingField {
        int lockOwnerx;

        public ObjectWithClashingField(int lockOwner) {
            this.lockOwnerx = lockOwner;
        }

        public int getThaLockOwner() {
            return lockOwnerx;
        }
    }

    @Test
    public void testContentOfFastAtomicObjectIsCopied(){
        testIncomplete();
    }
}
