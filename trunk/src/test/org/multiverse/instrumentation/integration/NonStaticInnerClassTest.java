package org.multiverse.instrumentation.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.commit;
import org.multiverse.api.GlobalStmInstance;
import org.multiverse.api.Handle;
import org.multiverse.api.Stm;
import org.multiverse.api.Transaction;
import org.multiverse.instrumentation.integration.Outer.Inner;

public class NonStaticInnerClassTest {
    private Stm stm;

    @Before
    public void setUp() {
        stm = GlobalStmInstance.getInstance();
    }

    @Test
    public void testAttachmentOfOuter() {
        int outer = 24;
        int inner = 1004;

        Outer original = new Outer(outer, inner);
        Handle<Outer> handle = commit(original);

        Transaction t = stm.startTransaction();
        Outer found = t.read(handle);
        assertEquals(outer, found.getValue());
        assertEquals(inner, found.getInner().getValue());
        assertSame(found, found.getInner().getOuter());
    }

    @Test
    public void testAttachementOfInner() {
        int outer = 24;
        int inner = 1004;

        Outer original = new Outer(outer, inner);

        Handle<Outer.Inner> handle = commit(original.getInner());

        Transaction t2 = stm.startTransaction();
        Inner found = t2.read(handle);
        assertEquals(inner, found.getValue());
        assertEquals(outer, found.getOuter().getValue());
    }

    @Test
    public void changeOnInnerClass() {
        int outer = 24;
        int inner = 1004;

        Outer original = new Outer(outer, inner);
        Handle<Outer> handle = commit(original);

        Transaction t = stm.startTransaction();
        Outer found = t.read(handle);
        found.getInner().inc();
        t.commit();

        Transaction t2 = stm.startTransaction();
        Outer found2 = t2.read(handle);
        assertEquals(outer, found.getValue());
        assertEquals(inner + 1, found.getInner().getValue());
    }
}
