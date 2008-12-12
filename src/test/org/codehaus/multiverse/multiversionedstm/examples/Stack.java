package org.codehaus.multiverse.multiversionedstm.examples;

import static org.codehaus.multiverse.TransactionMethods.retry;
import org.codehaus.multiverse.multiversionedstm.DehydratedStmObject;
import org.codehaus.multiverse.multiversionedstm.StmObject;
import org.codehaus.multiverse.core.Transaction;
import org.codehaus.multiverse.util.iterators.EmptyIterator;

import java.util.Iterator;

public class Stack<E> implements StmObject {

    private Node<E> head;

    public void push(E item) {
        if (item == null) throw new NullPointerException();
        head = new Node(item, head);
    }

    public E peek() {
        if (head == null)
            return null;

        return removeTopItem();
    }

    public E pop() {
        if (head == null)
            retry();

        return removeTopItem();
    }

    private E removeTopItem() {
        Node<E> oldHead = head;
        head = head.parent;
        return oldHead.value;
    }

    public int size() {
        return head == null ? 0 : head.size();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public static class Node<E> {
        final E value;
        final Node parent;
        final int size;

        Node(E value, Node prev) {
            this.value = value;
            this.parent = prev;
            this.size = parent == null ? 1 : prev.size;
        }

        int size() {
            return size;
        }
    }

    //================== generated  ======================

    //generated
    private Node head_initial;
    private long handle;
    private Transaction transaction;
    private DehydratedStack initialStack;

    public DehydratedStmObject ___getInitialDehydratedStmObject() {
        return initialStack;
    }

    public Iterator<StmObject> ___loadedMembers() {
        //todo: alle elementen van de stack moeten bij lang worden gelopen
        return EmptyIterator.INSTANCE;
    }

    public void ___onAttach(Transaction transaction, long handle) {
        this.transaction = transaction;
        this.handle = handle;
    }

    public Transaction ___getTransaction() {
        return transaction;
    }

    public long ___getHandle() {
        return handle;
    }

    public DehydratedStack ___dehydrate() {
        return new DehydratedStack(this);
    }

    public boolean ___isDirty() {
        if (initialStack == null)
            return true;

        if (head != head_initial)
            return true;

        return false;
    }

    public static class DehydratedStack extends DehydratedStmObject {
        private final Node head;

        public DehydratedStack(Stack stack) {
            super(stack.handle);
            this.head = stack.head;
        }

        public Iterator<Long> members() {
            throw new RuntimeException();
        }

        public Stack hydrate(Transaction transaction) {
            Stack stack = new Stack();
            stack.head = head;
            stack.head_initial = head;
            stack.transaction = transaction;
            stack.handle = getHandle();
            stack.initialStack = this;
            return stack;
        }
    }
}
