package org.codehaus.multiverse.multiversionedstm.examples;

import org.codehaus.multiverse.core.RetryError;
import org.codehaus.multiverse.core.Stm;
import org.codehaus.multiverse.core.Transaction;
import org.codehaus.multiverse.multiversionedstm.MultiversionedStm;
import static org.junit.Assert.*;
import org.junit.Test;

import java.util.Collections;
import static java.util.Collections.reverse;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class StackTest {

    @Test
    public void testBasicFunctionality() {
        Stack stack = new Stack();
        assertEquals(0, stack.size());

        String item1 = "1";
        String item2 = "2";
        String item3 = "3";

        stack.push(item1);
        assertEquals(1, stack.size());

        stack.push(item2);
        assertEquals(2, stack.size());

        stack.push(item3);
        assertEquals(3, stack.size());

        Object result = stack.pop();
        assertSame(item3, result);
        assertEquals(2, stack.size());

        result = stack.pop();
        assertSame(item2, result);
        assertEquals(1, stack.size());

        result = stack.pop();
        assertSame(item1, result);
        assertEquals(0, stack.size());
    }

    @Test
    public void testAsList() {
        List itemList = createItemList(10);

        Stack stack = new Stack(itemList.iterator());
        reverse(itemList);

        assertEquals(itemList, stack.asList());
    }

    @Test
    public void testFreshStackIsDirty() {
        Stack stack = new Stack();
        assertTrue(stack.___isDirty());
    }

    @Test
    public void testHydratedStackWithOnlyReadsIsNotDirty() {
        Stack stack = new Stack();

        Stm stm = new MultiversionedStm();
        Transaction t = stm.startTransaction();
        long handle = t.attachAsRoot(stack);
        t.commit();

        Transaction t2 = stm.startTransaction();
        Stack hydratedStack = (Stack) t2.read(handle);
        assertFalse(hydratedStack.___isDirty());

        //do readonly operation
        int size = stack.size();
        //make sure that it isn't dirty.
        assertFalse(hydratedStack.___isDirty());
    }

    @Test
    public void testHydratedStackWithWritesIsDirty() {
        Stack stack = new Stack();

        Stm stm = new MultiversionedStm();
        Transaction t = stm.startTransaction();
        long handle = t.attachAsRoot(stack);
        t.commit();

        Transaction t2 = stm.startTransaction();
        Stack hydratedStack = (Stack) t2.read(handle);
        hydratedStack.push(10);
        assertTrue(hydratedStack.___isDirty());
    }

    @Test
    public void testMultipleTransactions() {
        Stack stack = new Stack();

        Stm stm = new MultiversionedStm();
        Transaction t = stm.startTransaction();
        long handle = t.attachAsRoot(stack);
        t.commit();

        List<Integer> itemsToPush = createItemList(100);

        for (Integer item : itemsToPush) {
            t = stm.startTransaction();
            stack = (Stack) t.read(handle);
            stack.push(item);
            t.commit();
        }

        t = stm.startTransaction();
        stack = (Stack) t.read(handle);
        List<Integer> drainedItems = stack.drain();
        Collections.reverse(drainedItems);
        assertEquals(itemsToPush, drainedItems);
    }

    private List<Integer> createItemList(int size) {
        List<Integer> result = new LinkedList<Integer>();
        for (int k = 0; k < size; k++)
            result.add(k);
        return result;
    }

    @Test(expected = RetryError.class)
    public void testPoppingFromEmptyStack() {
        Stack stack = new Stack();
        stack.pop();
    }

    @Test
    public void testOnAttach() {
        MultiversionedStm stm = new MultiversionedStm();
        Transaction t = stm.startTransaction();
        Stack stack = new Stack();

        stack.___onAttach(t);

        assertSame(t, stack.___getTransaction());
    }

    @Test
    public void testDehydratedAndHydrateEmptyStack() {
        testDehydrateAndHydrateStack(new Stack());
    }

    @Test
    public void testDehydratedAndHydrateNonEmptyStack() {
        Stack stack = new Stack();
        stack.push("1");
        stack.push("2");
        stack.push("3");
        testDehydrateAndHydrateStack(stack);
    }

    private void testDehydrateAndHydrateStack(Stack originalStack) {
        MultiversionedStm stm = new MultiversionedStm();
        Transaction t1 = stm.startTransaction();
        t1.attachAsRoot(originalStack);
        t1.commit();

        Transaction t2 = stm.startTransaction();
        Stack hydratedStack = (Stack) t2.read(originalStack.___getHandle());
        assertEquals(originalStack, hydratedStack);
    }

    @Test
    public void testFreshStackHasNoTransaction() {
        Stack stack = new Stack();
        assertNull(stack.___getTransaction());
    }

    @Test
    public void testGetHandle() {
        Stack stack = new Stack();
        assertFalse(stack.___getHandle() == 0);
    }

    @Test
    public void testStackIsNotImmutable() {
        Stack stack = new Stack();
        assertFalse(stack.___isImmutable());
    }

    @Test
    public void testStackIsVeryCheapToWrite() {
        List<Integer> items = createItemList(100000);

        MultiversionedStm stm = new MultiversionedStm();
        MultiversionedStm.MultiversionedTransaction t = stm.startTransaction();
        long handle = t.attachAsRoot(new Stack<Integer>(items.iterator()));
        t.commit();

        assertEquals(1, t.getWriteCount());
    }

    @Test
    public void testStackIsVeryCheapToRead() {
        List<Integer> items = createItemList(100000);

        MultiversionedStm stm = new MultiversionedStm();
        Transaction t = stm.startTransaction();
        long handle = t.attachAsRoot(new Stack<Integer>(items.iterator()));
        t.commit();

        MultiversionedStm.MultiversionedTransaction t2 = stm.startTransaction();
        Stack<Integer> stack = (Stack<Integer>) t2.read(handle);
        List<Integer> found = stack.drain();

        assertEquals(1, t2.getHydratedObjectCount());
    }

    // ================= getFreshOrLoadedStmMembers ================

    @Test
    public void testGetFreshOrLoadedStmMembers_freshEmptyStack() {
        Stack stack = new Stack();
        Iterator it = stack.___getFreshOrLoadedStmMembers();
        assertFalse(it.hasNext());
    }

    @Test
    public void testGetFreshOrLoadedStmMembers_freshNonEmptyStack() {
        Stack stack = new Stack();
        stack.push(10);

        Iterator it = stack.___getFreshOrLoadedStmMembers();
        assertFalse(it.hasNext());
    }
}
