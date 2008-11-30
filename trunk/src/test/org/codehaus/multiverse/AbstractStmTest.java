package org.codehaus.multiverse;

import junit.framework.TestCase;
import org.codehaus.multiverse.transaction.Transaction;
import org.codehaus.multiverse.transaction.NoSuchObjectException;

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

    public void sleepRandom(long ms) {
        sleep((long) (Math.random() * ms));
    }

    public void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            Thread.interrupted();
            throw new RuntimeException(ex);
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
