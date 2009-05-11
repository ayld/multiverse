package org.multiverse.multiversionedstm.examples;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Handle;
import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.MultiversionedStm;

public class ExampleStackTest {
    private MultiversionedStm stm;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
    }

    @Test
    public void dematerializeAndMaterializedNonEmptyStack() {
        ExampleStack stack = new ExampleStack();
        stack.push(10);
        stack.push(20);
        stack.push(30);
        stack.push(40);

        Transaction t1 = stm.startTransaction();
        Handle<ExampleStack> handle = t1.attach(stack);
        t1.commit();

        Transaction t2 = stm.startTransaction();
        ExampleStack foundStack = t2.read(handle);
        assertEquals(40, foundStack.pop());
        assertEquals(30, foundStack.pop());
        assertEquals(20, foundStack.pop());
        assertEquals(10, foundStack.pop());
        assertTrue(foundStack.isEmpty());
    }

    @Test
    public void dematerializeAndMaterializedEmptyStack() {
        ExampleStack stack = new ExampleStack();

        Transaction t1 = stm.startTransaction();
        Handle<ExampleStack> handle = t1.attach(stack);
        t1.commit();

        Transaction t2 = stm.startTransaction();
        ExampleStack foundStack = t2.read(handle);
        assertTrue(foundStack.isEmpty());
    }
}