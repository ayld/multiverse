package org.codehaus.multiverse.multiversionedstm.examples;

import org.codehaus.multiverse.api.Stm;
import org.codehaus.multiverse.api.Transaction;
import org.codehaus.multiverse.api.exceptions.RetryError;
import org.codehaus.multiverse.multiversionedstm.MultiversionedStm;
import static org.junit.Assert.*;
import org.junit.Test;

import static java.util.Arrays.asList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class QueueTest {

    @Test
    public void testBasicUsage() {
        Queue<String> queue = new Queue();
        assertTrue(queue.isEmpty());

        String item1 = "1";
        queue.push(item1);
        assertEquals(1, queue.size());
        assertEquals(asList(item1), queue.asList());

        String item2 = "2";
        queue.push(item2);
        assertEquals(2, queue.size());
        assertEquals(asList(item2, item1), queue.asList());

        String item3 = "3";
        queue.push(item3);
        assertEquals(3, queue.size());
        assertEquals(asList(item3, item2, item1), queue.asList());

        assertSame(item1, queue.pop());
        assertEquals(2, queue.size());
        assertEquals(asList(item3, item2), queue.asList());

        String item4 = "4";
        queue.push(item4);
        assertEquals(3, queue.size());
        assertEquals(asList(item4, item3, item2), queue.asList());

        String item5 = "5";
        queue.push(item5);
        assertEquals(4, queue.size());
        assertEquals(asList(item5, item4, item3, item2), queue.asList());

        assertSame(item2, queue.pop());
        assertEquals(3, queue.size());
        assertEquals(asList(item5, item4, item3), queue.asList());

        assertSame(item3, queue.pop());
        assertEquals(2, queue.size());
        assertEquals(asList(item5, item4), queue.asList());

        assertSame(item4, queue.pop());
        assertEquals(1, queue.size());
        assertEquals(asList(item5), queue.asList());

        assertSame(item5, queue.pop());
        assertEquals(0, queue.size());
        assertEquals(asList(), queue.asList());
    }

    @Test
    public void testPeek() {
        Queue queue = new Queue();
        assertNull(queue.peek());

        String item1 = "1";
        queue.push(item1);
        assertSame(item1, queue.peek());
        assertNull(queue.peek());

        String item2 = "2";
        String item3 = "3";
        queue.push(item2);
        queue.push(item3);

        assertSame(item2, queue.peek());
        String item4 = "4";
        queue.push(item4);
        assertSame(item3, queue.peek());
        assertSame(item4, queue.peek());
        assertNull(queue.peek());
    }

    @Test
    public void test() {
        Queue queue = new Queue(1);
        queue.push("foo");

        try {
            queue.push("bar");
            fail();
        } catch (RetryError er) {
            assertEquals(1, queue.size());
        }
    }

    @Test(expected = RetryError.class)
    public void testPoppingFromEmptyQueue() {
        Queue queue = new Queue();
        queue.pop();
    }

    @Test
    public void testIsDirtyFreshEmptyQueue() {
        Queue queue = new Queue();
        assertTrue(queue.___isDirtyIgnoringStmMembers());
    }

    @Test
    public void testIsDirtyFreshNonEmptyQueue() {
        Queue queue = new Queue();
        queue.push("10");
        assertTrue(queue.___isDirtyIgnoringStmMembers());
    }

    @Test
    public void testHydratedReadonlyQueueIsNotDirty() {
        Stm stm = new MultiversionedStm();
        long handle = atomicInsertQueue(stm);

        Transaction t2 = stm.startTransaction();
        Queue queue = (Queue) t2.read(handle);
        //readonly operation
        queue.size();
        assertFalse(queue.___isDirtyIgnoringStmMembers());
    }

    /**
     * The queue will never be dirty because it doesn't contain any mutable state itself, all mutable state
     * is stored in the stacks.
     */
    @Test
    public void testHydratedUpdatedQueueIsNotDirty() {
        Stm stm = new MultiversionedStm();
        long handle = atomicInsertQueue(stm);

        Transaction t2 = stm.startTransaction();
        Queue queue = (Queue) t2.read(handle);
        //readonly operation
        queue.push("10");
        assertFalse(queue.___isDirtyIgnoringStmMembers());
    }

    private long atomicInsertQueue(Stm stm) {
        Transaction t = stm.startTransaction();
        long handle = t.attachAsRoot(new Queue());
        t.commit();
        return handle;
    }

    @Test
    public void testDehydratedAndHydrateEmptyQueue() {
        testDehydrateAndHydrateQueue(new Queue());
    }

    @Test
    public void testDehydrateEmptyQueue() {
        Queue queue = new Queue();

        MultiversionedStm stm = new MultiversionedStm();
        MultiversionedStm.MultiversionedTransactionImpl t = stm.startTransaction();
        t.attachAsRoot(queue);
        t.commit();

        assertEquals(3, t.getWriteCount());
    }

    @Test
    public void testDehydratedAndHydrateNonEmptyQueue() {
        Queue queue = new Queue();
        queue.push("1");
        queue.push("2");
        queue.push("3");
        testDehydrateAndHydrateQueue(queue);
    }

    private void testDehydrateAndHydrateQueue(Queue originalQueue) {
        MultiversionedStm stm = new MultiversionedStm();
        Transaction t1 = stm.startTransaction();
        t1.attachAsRoot(originalQueue);
        t1.commit();

        Transaction t2 = stm.startTransaction();
        Queue hydratedQueue = (Queue) t2.read(originalQueue.___getHandle());
        assertEquals(originalQueue, hydratedQueue);
    }

    @Test
    public void testMultipleTransactions() {
        Queue<Integer> queue = new Queue();

        Stm stm = new MultiversionedStm();
        Transaction t = stm.startTransaction();
        long handle = t.attachAsRoot(queue);
        t.commit();

        List<Integer> itemsToPush = createItemList(100);

        for (Integer item : itemsToPush) {
            t = stm.startTransaction();
            queue = (Queue<Integer>) t.read(handle);
            queue.push(item);
            t.commit();
        }

        t = stm.startTransaction();
        queue = (Queue<Integer>) t.read(handle);
        List<Integer> drainedItems = queue.asList();
        Collections.reverse(drainedItems);
        assertEquals(itemsToPush, drainedItems);
    }

    private List<Integer> createItemList(int size) {
        List<Integer> result = new LinkedList<Integer>();
        for (int k = 0; k < size; k++)
            result.add(k);
        return result;
    }
}


