package org.codehaus.multiverse.multiversionedstm.examples;

import org.codehaus.multiverse.core.Transaction;
import org.codehaus.multiverse.multiversionedheap.AbstractDeflated;
import org.codehaus.multiverse.multiversionedstm.HandleGenerator;
import org.codehaus.multiverse.multiversionedstm.MyTransaction;
import org.codehaus.multiverse.multiversionedstm.StmObject;
import static org.codehaus.multiverse.multiversionedstm.TransactionMethods.retry;
import org.codehaus.multiverse.util.iterators.ArrayIterator;

import static java.util.Collections.reverse;
import java.util.Iterator;
import java.util.List;

/**
 * Since the Queue itself is immutable (only the member stacks are mutable) dehydrated is very cheap (the previous
 * dehydratedQueue could be returned.
 *
 * @param <E>
 */
public class Queue<E> implements StmObject {

    private final Stack<E> readyToPopStack;
    private final Stack<E> pushedStack;
    private final int maxCapacity;

    public Queue(int maximumCapacity) {
        if (maximumCapacity < 1)
            throw new IllegalArgumentException();

        this.maxCapacity = maximumCapacity;

        //placed into the constructor.
        readyToPopStack = new Stack<E>();
        pushedStack = new Stack<E>();
        //generated
        handle = HandleGenerator.createHandle();
    }

    public Queue() {
        this(Integer.MAX_VALUE);
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public E peek() {
        E result = readyToPopStack.peek();
        if (result != null)
            return result;

        flipFromPushedToReadyToPop();
        return readyToPopStack.peek();
    }

    public E pop() {
        if (!readyToPopStack.isEmpty())
            return readyToPopStack.pop();

        flipFromPushedToReadyToPop();
        return readyToPopStack.pop();
    }

    private void flipFromPushedToReadyToPop() {
        while (!pushedStack.isEmpty()) {
            E item = pushedStack.pop();
            readyToPopStack.push(item);
        }
    }

    public void push(E value) {
        if (size() == maxCapacity)
            retry();

        pushedStack.push(value);
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public int size() {
        return readyToPopStack.size() + pushedStack.size();
    }

    public List<E> asList() {
        List<E> result = pushedStack.asList();
        List<E> popped = readyToPopStack.asList();
        reverse(popped);
        result.addAll(popped);
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

        if (!(thatObj instanceof Queue))
            return false;

        Queue that = (Queue) thatObj;
        if (that.size() != this.size())
            return false;

        List<E> thatList = that.asList();
        List<E> thisList = this.asList();
        return thatList.equals(thisList);
    }

    //================== generated =================

    private final long handle;
    private MyTransaction transaction;
    private DehydratedQueue initialDehydratedQueue;

    public Queue(DehydratedQueue dehydratedQueue, MyTransaction transaction) {
        this.handle = dehydratedQueue.___getHandle();
        this.transaction = transaction;
        this.initialDehydratedQueue = dehydratedQueue;

        this.readyToPopStack = (Stack) transaction.read(dehydratedQueue.readyToPopStackPtr);
        this.pushedStack = (Stack) transaction.read(dehydratedQueue.pushedStackPtr);
        this.maxCapacity = dehydratedQueue.maxCapacity;
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

    public Iterator<StmObject> ___getFreshOrLoadedStmMembers() {
        return new ArrayIterator<StmObject>(readyToPopStack, pushedStack);
    }

    public DehydratedQueue ___deflate(long commitVersion) {
        return new DehydratedQueue(this, commitVersion);
    }

    public boolean ___isImmutableObjectGraph() {
        //the stacks are mutable, so the queue is mutable.
        return false;
    }

    public boolean ___isDirtyIgnoringStmMembers() {
        //if the object has never been saved before, it is dirty by default.
        if (initialDehydratedQueue == null)
            return true;

        //since the queue has no other state than the stacks (and those are final) it is not dirty.
        //it is up to the stacks to do the dirty check
        return false;
    }

    public static class DehydratedQueue extends AbstractDeflated {
        private final long readyToPopStackPtr;
        private final long pushedStackPtr;
        private final int maxCapacity;

        DehydratedQueue(Queue queue, long commitVersion) {
            super(queue.___getHandle(), commitVersion);
            this.readyToPopStackPtr = queue.readyToPopStack.___getHandle();
            this.pushedStackPtr = queue.pushedStack.___getHandle();
            this.maxCapacity = queue.maxCapacity;
        }

        public Queue ___inflate(Transaction transaction) {
            //todo  cast should be removed
            return new Queue(this, (MyTransaction) transaction);
        }
    }
}
