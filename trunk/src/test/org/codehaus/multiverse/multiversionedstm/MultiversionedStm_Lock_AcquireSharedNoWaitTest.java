package org.codehaus.multiverse.multiversionedstm;

import static org.codehaus.multiverse.TestUtils.commit;
import org.codehaus.multiverse.api.LockMode;
import org.codehaus.multiverse.api.PessimisticLock;
import org.codehaus.multiverse.api.Transaction;
import org.codehaus.multiverse.api.TransactionId;
import org.codehaus.multiverse.multiversionedstm.examples.Stack;
import org.codehaus.multiverse.utils.Pair;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;

public class MultiversionedStm_Lock_AcquireSharedNoWaitTest {

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
        Pair<TransactionId, LockMode> lockInfo = lock.getLockInfo();
        assertNull(lockInfo.getA());
        assertEquals(LockMode.free, lockInfo.getB());
    }

    @Test
    public void lockIsFree() {
        long handle = commit(stm, new Stack());

        Transaction t = stm.startTransaction();
        Stack stack = (Stack) t.read(handle);
        PessimisticLock lock = t.getPessimisticLock(stack);

        lock.acquireSharedNoWait();

        Pair<TransactionId, LockMode> lockInfo = lock.getLockInfo();
        assertEquals(t.getId(), lockInfo.getA());
        assertEquals(LockMode.shared, lockInfo.getB());
        t.commit();
    }

    @Test
    public void lockIsSharedByOnlyCurrentTransaction() {
        long handle = commit(stm, new Stack());

        Transaction t = stm.startTransaction();
        Stack stack = (Stack) t.read(handle);
        PessimisticLock lock = t.getPessimisticLock(stack);
        lock.acquireSharedNoWait();

        lock.acquireSharedNoWait();

        Pair<TransactionId, LockMode> lockInfo = lock.getLockInfo();
        assertEquals(t.getId(), lockInfo.getA());
        assertEquals(LockMode.shared, lockInfo.getB());
        t.commit();
    }

    @Test
    public void lockIsExclusivelyOwnedByCurrentTransaction() {
        long handle = commit(stm, new Stack());

        Transaction t = stm.startTransaction();
        Stack stack = (Stack) t.read(handle);
        PessimisticLock lock = t.getPessimisticLock(stack);
        lock.acquireExclusiveNoWait();

        lock.acquireSharedNoWait();

        Pair<TransactionId, LockMode> lockInfo = lock.getLockInfo();
        assertEquals(t.getId(), lockInfo.getA());
        assertEquals(LockMode.exclusive, lockInfo.getB());
        t.commit();
    }

    public void acquireSharedNoWait_lockIsSharedByOtherTransactions() {
        //todo
    }

    public void acquireSharedNoWait_lockIsExclusivelyOwnedByOtherTransaction() {
        //todo
    }

    public void acquiredSharedLockIsReleasedWhenTransactionCompletes() {
        //todo
    }
}
