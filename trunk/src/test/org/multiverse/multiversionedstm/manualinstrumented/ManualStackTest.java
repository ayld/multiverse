package org.multiverse.multiversionedstm.manualinstrumented;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Handle;
import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.MultiversionedStm;

public class ManualStackTest {
    private MultiversionedStm stm;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
    }

    @Test
    public void dematerializeAndMaterializedNonEmptyStack() {
        ManualStack<Integer> stack = new ManualStack<Integer>();
        stack.push(10);
        stack.push(20);
        stack.push(30);
        stack.push(40);

        Transaction t1 = stm.startTransaction();
        Handle<ManualStack<Integer>> handle = t1.attach(stack);
        t1.commit();

        Transaction t2 = stm.startTransaction();
        ManualStack foundStack = t2.read(handle);
        assertEquals(40, foundStack.pop());
        assertEquals(30, foundStack.pop());
        assertEquals(20, foundStack.pop());
        assertEquals(10, foundStack.pop());
        assertTrue(foundStack.isEmpty());
    }

    @Test
    public void dematerializeAndMaterializedEmptyStack() {
        ManualStack stack = new ManualStack();

        Transaction t1 = stm.startTransaction();
        Handle<ManualStack> handle = t1.attach(stack);
        t1.commit();

        Transaction t2 = stm.startTransaction();
        ManualStack foundStack = t2.read(handle);
        assertTrue(foundStack.isEmpty());
    }
}