package org.codehaus.multiverse.multiversionedstm.examples;

import org.codehaus.multiverse.core.Transaction;
import org.codehaus.multiverse.multiversionedstm.DehydratedStmObject;
import org.codehaus.multiverse.multiversionedstm.HandleGenerator;
import org.codehaus.multiverse.multiversionedstm.StmObject;
import static org.codehaus.multiverse.multiversionedstm.TransactionMethods.retry;
import org.codehaus.multiverse.util.iterators.EmptyIterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Stack<E> implements StmObject {

    private Node<E> head;

    public Stack() {
        //generated
        handle = HandleGenerator.createHandle();
        initialStack = null;
    }

    public Stack(Iterator<E> it) {
        this();

        for (; it.hasNext();)
            push(it.next());
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
        return head == null ? 0 : head.size;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public List<E> drain() {
        List<E> result = new ArrayList<E>(size());
        E item;
        while ((item = peek()) != null)
            result.add(item);
        return result;
    }

    /**
     * Returns a List representation of the array with the last pushed element as the head of the list.
     */
    public List<E> asList() {
        List<E> result = new ArrayList<E>(size());
        Node<E> node = head;
        for (int k = 0; k < size(); k++) {
            result.add(node.value);
            node = node.parent;
        }
        return result;
    }

    @Override
    public int hashCode() {
        return asList().hashCode();
    }

    @Override
    public boolean equals(Object thatObj) {
        if (thatObj == this)
            return true;

        if (!(thatObj instanceof Stack))
            return false;

        Stack that = (Stack) thatObj;
        if (that.size() != this.size())
            return false;

        if (this.head == null)
            return that.head == null;

        return this.head.equals(that.head);
    }

    public static class Node<E> extends DehydratedStmObject implements StmObject {
        final E value;
        final Node parent;
        final int size;

        Node(E value, Node prev) {
            super(HandleGenerator.createHandle());
            this.value = value;
            this.parent = prev;
            this.size = parent == null ? 1 : prev.size + 1;
        }

        @Override
        public boolean equals(Object thatObj) {
            if (this == thatObj)
                return true;

            if (!(thatObj instanceof Node))
                return false;

            Node that = (Node) thatObj;
            if (!this.value.equals(that.value))
                return false;

            if (this.parent == null)
                return that.parent == null;

            return this.parent.equals(that.parent);
        }

        public void ___onAttach(Transaction transaction) {
            throw new RuntimeException();
        }

        public Transaction ___getTransaction() {
            throw new RuntimeException();
        }

        public boolean ___isDirty() {
            throw new RuntimeException();
        }

        public boolean ___isImmutable() {
            return true;
        }

        public long ___getHandle() {
            return getHandle();
        }

        public DehydratedStmObject ___dehydrate() {
            return this;
        }

        public Iterator<StmObject> ___getFreshOrLoadedStmMembers() {
            return EmptyIterator.INSTANCE;
        }

        public Iterator<Long> members() {
            throw new RuntimeException();
        }

        public StmObject hydrate(Transaction transaction) {
            return this;
        }
    }

    //================== generated  ======================

    private final long handle;
    private final DehydratedStack initialStack;
    private Transaction transaction;

    public Stack(DehydratedStack<E> dehydratedStack, Transaction transaction) {
        this.head = dehydratedStack.head;
        this.transaction = transaction;
        this.handle = dehydratedStack.getHandle();
        this.initialStack = dehydratedStack;
    }

    public Iterator<StmObject> ___getFreshOrLoadedStmMembers() {
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

    public DehydratedStack<E> ___dehydrate() {
        return new DehydratedStack<E>(this);
    }

    public boolean ___isImmutable() {
        return false;
    }

    public boolean ___isDirty() {
        //if the object has not been saved before, it is dirty by default.
        if (initialStack == null)
            return true;

        //if the head has changed, the stack is dirty.
        if (initialStack.head != head)
            return true;

        return false;
    }

    public static class DehydratedStack<E> extends DehydratedStmObject {
        private final Node<E> head;

        public DehydratedStack(Stack<E> stack) {
            super(stack.handle);
            this.head = stack.head;
        }

        public Iterator<Long> members() {
            throw new RuntimeException();
        }

        public Stack<E> hydrate(Transaction transaction) {
            return new Stack<E>(this, transaction);
        }
    }
}
