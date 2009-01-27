package org.codehaus.multiverse.multiversionedstm.examples;

import org.codehaus.multiverse.core.Transaction;
import org.codehaus.multiverse.multiversionedstm.DehydratedStmObject;
import org.codehaus.multiverse.multiversionedstm.HandleGenerator;
import org.codehaus.multiverse.multiversionedstm.StmObject;
import static org.codehaus.multiverse.multiversionedstm.TransactionMethods.retry;
import org.codehaus.multiverse.util.iterators.EmptyIterator;

import java.util.Iterator;

public class Stack<E> implements StmObject {

    private Node<E> head;

    public Stack() {
        //generated
        handle = HandleGenerator.create();
    }

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
            this.size = parent == null ? 1 : prev.size + 1;
        }

        int size() {
            return size;
        }
    }

    //================== generated  ======================

    private Node head_initial;
    private final long handle;
    private Transaction transaction;
    private DehydratedStack initialStack;

    public Stack(DehydratedStack dehydratedStack, Transaction transaction) {
        this.head = dehydratedStack.head;
        this.head_initial = dehydratedStack.head;
        this.transaction = transaction;
        this.handle = dehydratedStack.getHandle();
        this.initialStack = dehydratedStack;
    }

    public DehydratedStmObject ___getInitialDehydratedStmObject() {
        return initialStack;
    }

    public Iterator<StmObject> ___loadedMembers() {
        //todo: alle elementen van de stack moeten bij lang worden gelopen
        return EmptyIterator.INSTANCE;
    }

    public void ___onAttach(Transaction transaction) {
        this.transaction = transaction;
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
            return new Stack(this, transaction);
        }
    }
}
