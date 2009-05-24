package org.multiverse.instrumentation;

import org.multiverse.api.Handle;
import org.multiverse.api.Transaction;
import org.multiverse.api.annotations.Atomic;
import org.multiverse.api.annotations.TmEntity;
import org.multiverse.collections.*;
import org.multiverse.multiversionedstm.MultiversionedStm;

/**
 * A Main that runs some tests.
 * <p/>
 * will be removed in the future..
 */
public class TestRunningMain {

    public static void main(String[] args) {
        System.out.println("Main called");

        Account foo = new Account();
        foo.transferTo();
    }

    @TmEntity
    private static class Account {
        private int balance;

        @Atomic
        public Object transferTo() {
            return null;
        }
    }


    private static void testLinkedList() {
        LinkedList tree = new LinkedList();
        tree.add("a");
        tree.add("b");
        tree.add("c");

        MultiversionedStm stm = new MultiversionedStm();
        Transaction t = stm.startTransaction();
        Handle<LinkedList> handle = t.attach(tree);
        t.commit();

        Transaction t2 = stm.startTransaction();
        LinkedList found = t2.read(handle);
        System.out.println("found: " + found);
        found.clear();
        t2.commit();

        Transaction t3 = stm.startTransaction();
    }


    private static void testBTree() {
        BTree tree = new BTree();

        MultiversionedStm stm = new MultiversionedStm();
        Transaction t = stm.startTransaction();
        Handle<BTree> handle = t.attach(tree);
        t.commit();

        Transaction t2 = stm.startTransaction();
        BTree found = t2.read(handle);
        System.out.println("found: " + found);
        t2.commit();
    }

    private static void testLatch() {
        Latch latch = new Latch();
        latch.open();
        System.out.println("original " + latch);

        MultiversionedStm stm = new MultiversionedStm();
        Transaction t = stm.startTransaction();
        Handle<Latch> handle = t.attach(latch);
        t.commit();

        Transaction t2 = stm.startTransaction();
        Latch found = t2.read(handle);
        System.out.println("found: " + found);
        t2.commit();
    }

    private static void testQueue() {
        Queue queue = new Queue();

        MultiversionedStm stm = new MultiversionedStm();
        Transaction t = stm.startTransaction();
        Handle<Queue> handle = t.attach(queue);
        t.commit();
    }

    private static void testStack() {
        Stack stack = new Stack();

        MultiversionedStm stm = new MultiversionedStm();
        Transaction t = stm.startTransaction();
        Handle<Stack> handle = t.attach(stack);
        stack.push("item1");
        stack.push("item2");

        System.out.println("stack.size: " + stack.size());
        t.commit();

        Transaction t2 = stm.startTransaction();
        Stack foundStack = t2.read(handle);

        System.out.println("foundStack.size: " + foundStack.size());

        while (!foundStack.isEmpty()) {
            System.out.println("popped item: " + foundStack.pop() + " size = " + foundStack.size());
        }

        t2.commit();
    }
}
