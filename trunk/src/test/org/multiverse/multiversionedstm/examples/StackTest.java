package org.multiverse.multiversionedstm.examples;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Handle;
import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.MultiversionedStm;

public class StackTest {
    private MultiversionedStm stm;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
    }

    @Test
    public void dematerializeAndMaterializedNonEmptyStack() {
        Stack stack = new Stack();
        stack.push(10);
        stack.push(20);
        stack.push(30);
        stack.push(40);

        Transaction t1 = stm.startTransaction();
        Handle<Stack> handle = t1.attach(stack);
        t1.commit();

        Transaction t2 = stm.startTransaction();
        Stack foundStack = t2.read(handle);
        assertEquals(40, foundStack.pop());
        assertEquals(30, foundStack.pop());
        assertEquals(20, foundStack.pop());
        assertEquals(10, foundStack.pop());
        assertTrue(foundStack.isEmpty());
    }

    @Test
    public void dematerializeAndMaterializedEmptyStack() {
        Stack stack = new Stack();

        Transaction t1 = stm.startTransaction();
        Handle<Stack> handle = t1.attach(stack);
        t1.commit();

        Transaction t2 = stm.startTransaction();
        Stack foundStack = t2.read(handle);
        assertTrue(foundStack.isEmpty());
    }
}