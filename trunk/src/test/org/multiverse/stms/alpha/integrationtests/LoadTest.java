package org.multiverse.stms.alpha.integrationtests;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.annotations.AtomicMethod;
import org.multiverse.datastructures.refs.Ref;
import org.multiverse.stms.alpha.AlphaAtomicObject;
import org.multiverse.stms.alpha.AlphaStm;
import org.multiverse.stms.alpha.AlphaTranlocal;
import org.multiverse.stms.alpha.AlphaTransaction;
import org.multiverse.utils.GlobalStmInstance;
import static org.multiverse.utils.TransactionThreadLocal.getThreadLocalTransaction;

public class LoadTest {

    private AlphaStm stm;
    private static Ref<Integer> intRef;

    @Before
    public void setUp() {
        stm = new AlphaStm();
        GlobalStmInstance.set(stm);
        intRef = new Ref<Integer>(0);
    }

    @Test
    public void testUpdateLoad() {
        executeUpdate();
    }

    public static AlphaTranlocal getTranlocal() {
        AlphaTransaction t = (AlphaTransaction) getThreadLocalTransaction();
        return t.load((AlphaAtomicObject) ((Object) intRef));
    }

    @AtomicMethod
    public static void executeUpdate() {
        AlphaTranlocal tranlocal = getTranlocal();
        assertFalse(tranlocal.committed);
        assertEquals(0, (int) intRef.get());
    }

    @Test
    public void testReadonlyLoad() {
        executeReadonly();
    }

    @AtomicMethod(readonly = true)
    public static void executeReadonly() {
        assertTrue(getTranlocal().committed);
        assertEquals(0, (int) intRef.get());
    }
}
