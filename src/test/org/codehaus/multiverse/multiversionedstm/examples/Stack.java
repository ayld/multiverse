package org.codehaus.multiverse.multiversionedstm.examples;

import org.codehaus.multiverse.core.Transaction;
import org.codehaus.multiverse.multiversionedheap.AbstractDeflated;
import org.codehaus.multiverse.multiversionedheap.Deflated;
import org.codehaus.multiverse.multiversionedstm.HandleGenerator;
import org.codehaus.multiverse.multiversionedstm.MyTransaction;
import org.codehaus.multiverse.multiversionedstm.StmObject;
import static org.codehaus.multiverse.multiversionedstm.TransactionMethods.retry;
import org.codehaus.multiverse.util.iterators.EmptyIterator;
import org.codehaus.multiverse.util.iterators.PLongIterator;

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

    public static class Node<E> implements StmObject, Deflated {
        final E value;
        final Node parent;
        final int size;
        final long handle;

        Node(E value, Node prev) {
            this.handle = HandleGenerator.createHandle();
            this.value = value;
            this.parent = prev;
            this.size = parent == null ? 1 : prev.size + 1;
        }

        @Override
        public long ___getVersion() {
            throw new RuntimeException();
        }

        @Override
        public Deflated ___deflate(long version) {
            throw new RuntimeException();
        }

        @Override
        public PLongIterator ___memberHandles() {
            throw new RuntimeException();
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

        public void ___onAttach(MyTransaction transaction) {
            throw new RuntimeException();
        }

        public MyTransaction ___getTransaction() {
            throw new RuntimeException();
        }

        public boolean ___isDirtyIgnoringStmMembers() {
            throw new RuntimeException();
        }

        public boolean ___isImmutableObjectGraph() {
            return true;
        }

        public long ___getHandle() {
            return handle;
        }

        public Deflated ___deflate() {
            return this;
        }

        public Iterator<StmObject> ___getFreshOrLoadedStmMembers() {
            return EmptyIterator.INSTANCE;
        }

        public StmObject ___inflate(Transaction transaction) {
            return this;
        }
    }

    //================== generated  ======================

    private final long handle;
    private final DehydratedStack initialStack;
    private MyTransaction transaction;

    public Stack(DehydratedStack<E> dehydratedStack, MyTransaction transaction) {
        this.head = dehydratedStack.head;
        this.transaction = transaction;
        this.handle = dehydratedStack.___getHandle();
        this.initialStack = dehydratedStack;
    }

    public Iterator<StmObject> ___getFreshOrLoadedStmMembers() {
        //todo: at the moment the nodes are not returned.
        return EmptyIterator.INSTANCE;
    }

    public void ___onAttach(MyTransaction transaction) {
        this.transaction = transaction;
    }

    public MyTransaction ___getTransaction() {
        return transaction;
    }

    public long ___getHandle() {
        return handle;
    }

    public DehydratedStack<E> ___deflate(long commitVersion) {
        return new DehydratedStack<E>(this, commitVersion);
    }

    public boolean ___isImmutableObjectGraph() {
        return false;
    }

    public boolean ___isDirtyIgnoringStmMembers() {
        //if the object has not been saved before, it is dirty by default.
        if (initialStack == null)
            return true;

        //if the head has changed, the stack is dirty.
        if (initialStack.head != head)
            return true;

        return false;
    }

    public static class DehydratedStack<E> extends AbstractDeflated {
        private final Node<E> head;

        public DehydratedStack(Stack<E> stack, long commitVersion) {
            super(stack.handle, commitVersion);
            this.head = stack.head;
        }

        public Stack<E> ___inflate(Transaction transaction) {
            //todo: remove cast
            return new Stack<E>(this, (MyTransaction) transaction);
        }
    }
}
