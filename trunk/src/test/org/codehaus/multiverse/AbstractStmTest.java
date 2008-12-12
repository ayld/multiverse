package org.codehaus.multiverse;

import junit.framework.TestCase;
import org.codehaus.multiverse.core.Transaction;
import org.codehaus.multiverse.core.NoSuchObjectException;
import org.codehaus.multiverse.core.Stm;

public abstract class AbstractStmTest<S extends Stm> extends TestCase {

    protected S stm;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        stm = createStm();
    }

    public abstract S createStm();

    public void assertNoObjectInStm(long ptr) {
        Transaction t = stm.startTransaction();
        try {
            Object x= t.read(ptr);
            fail();
        } catch (NoSuchObjectException ex) {
        } finally {
            t.commit();
        }
    }

    public long atomicInsert(Object obj) {
        Transaction t = stm.startTransaction();
        try {
            long ptr = t.attachAsRoot(obj);
            t.commit();
            return ptr;
        } catch (RuntimeException ex) {
            t.abort();
            throw ex;
        }
    }
}
