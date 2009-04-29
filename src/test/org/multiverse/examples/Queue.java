package org.multiverse.examples;

import org.multiverse.api.Originator;
import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.DefaultOriginator;
import org.multiverse.multiversionedstm.DematerializedObject;
import org.multiverse.multiversionedstm.MaterializedObject;
import org.multiverse.multiversionedstm.MemberTracer;
import static org.multiverse.multiversionedstm.MultiversionedStmUtils.retry;

import static java.lang.String.format;
import static java.util.Collections.reverse;
import java.util.List;

public final class Queue<E> implements MaterializedObject {

    private final Stack<E> readyToPopStack;
    private final Stack<E> pushedStack;
    private final int maxCapacity;

    public Queue() {
        this(Integer.MAX_VALUE);
    }

    public Queue(int maximumCapacity) {
        if (maximumCapacity < 1)
            throw new IllegalArgumentException();
        this.maxCapacity = maximumCapacity;

        //moved into the constructor.
        this.readyToPopStack = new Stack<E>();
        this.pushedStack = new Stack<E>();
        this.originator = new DefaultOriginator<Queue<E>>();
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public E tryPop() {
        E result = readyToPopStack.tryPop();
        if (result != null)
            return result;

        flip();
        return readyToPopStack.tryPop();
    }

    public E pop() {
        if (!readyToPopStack.isEmpty()) {
            return readyToPopStack.pop();
        }

        flip();
        return readyToPopStack.pop();
    }

    private void flip() {
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

    public String toDebugString() {
        return format("ReadyToPopStack=%s and PushedStack=%s", readyToPopStack, pushedStack);
    }

    @Override
    public String toString() {
        return asList().toString();
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

        Queue<E> that = (Queue<E>) thatObj;
        if (that.size() != this.size())
            return false;

        List<E> thatList = that.asList();
        List<E> thisList = this.asList();
        return thatList.equals(thisList);
    }

    //================== generated =================

    private DematerializedQueue<E> lastDematerialized;
    private final Originator<Queue<E>> originator;

    public Queue(DematerializedQueue<E> dematerializedQueue, Transaction transaction) {
        this.lastDematerialized = dematerializedQueue;
        this.originator = dematerializedQueue.originator;
        this.readyToPopStack = transaction.readUnmanaged(dematerializedQueue.readyToPopStackOriginator);
        this.pushedStack = transaction.readUnmanaged(dematerializedQueue.pushedStackOriginator);
        this.maxCapacity = dematerializedQueue.maxCapacity;
    }

    private MaterializedObject nextInChain;

    @Override
    public MaterializedObject getNextInChain() {
        return nextInChain;
    }

    @Override
    public void setNextInChain(MaterializedObject next) {
        this.nextInChain = next;
    }

    @Override
    public void memberTrace(MemberTracer memberTracer) {
        memberTracer.onMember(readyToPopStack);
        memberTracer.onMember(pushedStack);
    }

    @Override
    public Originator<Queue<E>> getOriginator() {
        return originator;
    }

    @Override
    public boolean isDirty() {
        if (lastDematerialized == null)
            return true;

        return false;
    }

    public DematerializedQueue<E> dematerialize() {
        return lastDematerialized = new DematerializedQueue<E>(this);
    }

    public static class DematerializedQueue<E> implements DematerializedObject {
        private final Originator<Stack<E>> readyToPopStackOriginator;
        private final Originator<Stack<E>> pushedStackOriginator;
        private final Originator<Queue<E>> originator;
        private final int maxCapacity;

        DematerializedQueue(Queue<E> queue) {
            this.originator = queue.originator;
            this.readyToPopStackOriginator = queue.readyToPopStack.getOriginator();
            this.pushedStackOriginator = queue.pushedStack.getOriginator();
            this.maxCapacity = queue.maxCapacity;
        }

        @Override
        public Originator<Queue<E>> getOriginator() {
            return originator;
        }

        @Override
        public Queue rematerialize(Transaction t) {
            return new Queue<E>(this, t);
        }
    }
}
