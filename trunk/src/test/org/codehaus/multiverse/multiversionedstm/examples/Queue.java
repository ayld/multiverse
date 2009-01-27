package org.codehaus.multiverse.multiversionedstm.examples;

import org.codehaus.multiverse.core.Transaction;
import org.codehaus.multiverse.multiversionedstm.DehydratedStmObject;
import org.codehaus.multiverse.multiversionedstm.HandleGenerator;
import org.codehaus.multiverse.multiversionedstm.StmObject;
import org.codehaus.multiverse.util.iterators.ArrayIterator;

import java.util.Iterator;

public class Queue<E> implements StmObject {

    private Stack<E> readyToPopStack = new Stack<E>();
    private Stack<E> pushedStack = new Stack<E>();

    public Queue() {
        //generated
        handle = HandleGenerator.create();
    }

    public E pop() {
        if (!readyToPopStack.isEmpty())
            return readyToPopStack.pop();

        while (!pushedStack.isEmpty()) {
            E item = pushedStack.pop();
            readyToPopStack.push(item);
        }

        return readyToPopStack.pop();
    }

    public void push(E value) {
        pushedStack.push(value);
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public int size() {
        return readyToPopStack.size() + pushedStack.size();
    }

    //================== generated =================

    private final long handle;
    private Transaction transaction;
    private DehydratedQueue initialDehydratedQueue;

    public Queue(DehydratedQueue dehydratedQueue, Transaction transaction) {
        this.handle = dehydratedQueue.getHandle();
        this.transaction = transaction;

        this.readyToPopStack = (Stack) transaction.read(dehydratedQueue.readyToPopStackPtr);
        this.pushedStack = (Stack) transaction.read(dehydratedQueue.pushedStackPtr);
        this.initialDehydratedQueue = dehydratedQueue;
    }

    public DehydratedStmObject ___getInitialDehydratedStmObject() {
        return initialDehydratedQueue;
    }

    public void ___onAttach(Transaction transaction) {
        this.transaction = transaction;
    }

    public Transaction ___getTransaction() {
        return transaction;
    }

    public Iterator<StmObject> ___loadedMembers() {
        return new ArrayIterator<StmObject>(readyToPopStack, pushedStack);
    }

    public long ___getHandle() {
        return handle;
    }

    public DehydratedStmObject ___dehydrate() {
        return new DehydratedQueue(this);
    }

    public boolean ___isDirty() {
        if (initialDehydratedQueue == null)
            return true;

        return false;
    }

    public static class DehydratedQueue extends DehydratedStmObject {
        private final long readyToPopStackPtr;
        private final long pushedStackPtr;

        DehydratedQueue(Queue queue) {
            super(queue.___getHandle());
            this.readyToPopStackPtr = queue.readyToPopStack.___getHandle();
            this.pushedStackPtr = queue.pushedStack.___getHandle();
        }

        public Iterator<Long> members() {
            throw new RuntimeException();
        }

        public Queue hydrate(Transaction transaction) {
            return new Queue(this, transaction);
        }
    }
}
