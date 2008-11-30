package org.codehaus.multiverse.multiversionedstm.examples;

import org.codehaus.multiverse.multiversionedstm.DehydratedStmObject;
import org.codehaus.multiverse.multiversionedstm.*;
import org.codehaus.multiverse.util.iterators.ArrayIterator;
import org.codehaus.multiverse.transaction.Transaction;

import java.util.Iterator;

public class Queue<E> implements StmObject {

    private Stack<E> readyToPopStack = new Stack<E>();
    private Stack<E> pushedStack = new Stack<E>();

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

    private long ptr;
    private Transaction transaction;
    private DehydratedQueue initialDehydratedQueue;

    public DehydratedStmObject ___getInitialDehydratedStmObject() {
        return initialDehydratedQueue;
    }

    public void ___onAttach(Transaction transaction) {
        this.transaction = transaction;
    }

    public Transaction ___getTransaction() {
        return transaction;
    }

    public Iterator<StmObject> ___directReferencedIterator() {
        return new ArrayIterator<StmObject>(readyToPopStack, pushedStack);
    }

    public long ___getHandle() {
        return ptr;
    }

    public void ___setHandle(long ptr) {
        this.ptr = ptr;
    }

    public DehydratedStmObject ___dehydrate(long version) {
        return new DehydratedQueue(this,version);
    }

    public boolean ___isDirty() {
        if(initialDehydratedQueue == null)
            return true;

        return false;
    }

    public static class DehydratedQueue extends DehydratedStmObject {
        private final long readyToPopStackPtr;
        private final long pushedStackPtr;

        DehydratedQueue(Queue queue, long version) {
            super(queue.___getHandle(), version);
            this.readyToPopStackPtr = queue.readyToPopStack.___getHandle();
            this.pushedStackPtr = queue.pushedStack.___getHandle();
        }

        public Iterator<Long> getDirect() {
            throw new RuntimeException();
        }

        public Queue hydrate(Transaction transaction) {
            Queue queue = new Queue();
            queue.ptr = getHandle();
            queue.transaction = transaction;
            queue.readyToPopStack = (Stack) transaction.read(readyToPopStackPtr);
            queue.pushedStack = (Stack) transaction.read(pushedStackPtr);
            queue.initialDehydratedQueue = this;
            return queue;
        }
    }
}
