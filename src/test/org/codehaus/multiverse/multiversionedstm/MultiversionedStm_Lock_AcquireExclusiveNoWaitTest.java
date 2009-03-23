package org.codehaus.multiverse.multiversionedstm;

import static org.codehaus.multiverse.TestUtils.commit;
import org.codehaus.multiverse.api.LockMode;
import org.codehaus.multiverse.api.PessimisticLock;
import org.codehaus.multiverse.api.Transaction;
import org.codehaus.multiverse.api.TransactionId;
import org.codehaus.multiverse.api.exceptions.LockOwnedByOtherTransactionException;
import org.codehaus.multiverse.multiversionedstm.examples.Stack;
import org.codehaus.multiverse.utils.Pair;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

public class MultiversionedStm_Lock_AcquireExclusiveNoWaitTest {

    private MultiversionedStm stm;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
    }

    @Test
    public void freeLock() {
        long handle = commit(stm, new Stack());

        Transaction t = stm.startTransaction();
        Stack stack = (Stack) t.read(handle);
        PessimisticLock lock = t.getPessimisticLock(stack);

        lock.acquireExclusiveNoWait();

        Pair<TransactionId, LockMode> lockInfo = lock.getLockInfo();
        assertEquals(t.getId(), lockInfo.getA());
        assertEquals(LockMode.exclusive, lockInfo.getB());
        t.commit();
    }

    @Test
    public void sharedLock_TransactionIsExclusiveOwner() {
        long handle = commit(stm, new Stack());

        Transaction t = stm.startTransaction();
        Stack stack = (Stack) t.read(handle);
        PessimisticLock lock = t.getPessimisticLock(stack);
        lock.acquireSharedNoWait();

        //now lets do it again, and everything should still be alright.
        lock.acquireExclusiveNoWait();

        Pair<TransactionId, LockMode> lockInfo = lock.getLockInfo();
        assertEquals(t.getId(), lockInfo.getA());
        assertEquals(LockMode.exclusive, lockInfo.getB());
        t.commit();
    }

    @Test
    public void sharedLock_otherTransactionsAreOwner() {
        //todo
    }

    @Test
    public void sharedLock_otherTransactionsAreAlsoOwner() {
        //todo
    }

    @Test
    public void exclusiveLock_ThisTransactionIsOwner() {
        long handle = commit(stm, new Stack());

        Transaction t = stm.startTransaction();
        Stack stack = (Stack) t.read(handle);
        PessimisticLock lock = t.getPessimisticLock(stack);
        lock.acquireExclusiveNoWait();

        //now lets do it again, and everything should still be alright.
        lock.acquireExclusiveNoWait();

        Pair<TransactionId, LockMode> lockInfo = lock.getLockInfo();
        assertEquals(t.getId(), lockInfo.getA());
        assertEquals(LockMode.exclusive, lockInfo.getB());
        t.commit();
    }

    @Test
    public void exclusiveLock_OtherTransactionIsOwner() {
        long handle = commit(stm, new Stack());

        Transaction t1 = stm.startTransaction();
        PessimisticLock lock1 = t1.getPessimisticLock(t1.read(handle));
        lock1.acquireExclusiveNoWait();

        Transaction t2 = stm.startTransaction();
        PessimisticLock lock2 = t2.getPessimisticLock(t2.read(handle));

        //now lets do it again, and everything should still be alright.
        try {
            lock2.acquireExclusiveNoWait();
            fail();
        } catch (LockOwnedByOtherTransactionException ex) {

        }

        Pair<TransactionId, LockMode> lockInfo = lock2.getLockInfo();
        assertEquals(t1.getId(), lockInfo.getA());
        assertEquals(LockMode.exclusive, lockInfo.getB());
        t1.commit();
        t2.commit();
    }
}
