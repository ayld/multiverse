package org.multiverse.multiversionedstm.examples;

import org.multiverse.api.LazyReference;
import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.*;
import static org.multiverse.multiversionedstm.MultiversionedStmUtils.retry;

import static java.lang.String.format;
import static java.util.Collections.reverse;
import java.util.List;

public final class Queue<E> implements MaterializedObject {

    private Stack<E> readyToPopStack;
    private Stack<E> pushedStack;
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
        this.handle = new DefaultHandle<Queue<E>>();
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public E tryPop() {
        ensureReadyToPopStackLoaded();

        E result = readyToPopStack.tryPop();
        if (result != null)
            return result;

        flip();
        return readyToPopStack.tryPop();
    }

    public E pop() {
        ensureReadyToPopStackLoaded();

        if (!readyToPopStack.isEmpty()) {
            return readyToPopStack.pop();
        }

        flip();
        return readyToPopStack.pop();
    }

    private void flip() {
        ensureReadyToPopStackLoaded();
        ensurePushedStackLoaded();

        while (!pushedStack.isEmpty()) {
            E item = pushedStack.pop();
            readyToPopStack.push(item);
        }
    }

    public void push(E value) {
        ensurePushedStackLoaded();

        if (size() == maxCapacity)
            retry();

        pushedStack.push(value);
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public int size() {
        ensureReadyToPopStackLoaded();
        ensurePushedStackLoaded();
        return readyToPopStack.size() + pushedStack.size();
    }


    public List<E> asList() {
        ensureReadyToPopStackLoaded();
        ensurePushedStackLoaded();

        List<E> result = pushedStack.asList();
        List<E> popped = readyToPopStack.asList();
        reverse(popped);
        result.addAll(popped);
        return result;
    }

    public String toDebugString() {
        ensurePushedStackLoaded();
        ensureReadyToPopStackLoaded();

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
    private final MultiversionedHandle<Queue<E>> handle;
    private LazyReference<Stack<E>> pushedStackRef;
    private LazyReference<Stack<E>> readyToPopStackRef;

    public Queue(DematerializedQueue<E> dematerializedQueue, Transaction transaction) {
        this.lastDematerialized = dematerializedQueue;
        this.handle = dematerializedQueue.handle;
        this.readyToPopStackRef = transaction.readLazyAndUnmanaged(dematerializedQueue.readyToPopStackHandle);
        this.pushedStackRef = transaction.readLazyAndUnmanaged(dematerializedQueue.pushedStackHandle);
        this.maxCapacity = dematerializedQueue.maxCapacity;
    }

    private void ensurePushedStackLoaded() {
        if (pushedStackRef != null) {
            pushedStack = pushedStackRef.get();
            pushedStackRef = null;
        }
    }

    private void ensureReadyToPopStackLoaded() {
        if (readyToPopStackRef != null) {
            readyToPopStack = readyToPopStackRef.get();
            readyToPopStackRef = null;
        }
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
    public void walkMaterializedMembers(MemberWalker memberWalker) {
        if (readyToPopStack != null) memberWalker.onMember(readyToPopStack);
        if (pushedStack != null) memberWalker.onMember(pushedStack);
    }

    @Override
    public MultiversionedHandle<Queue<E>> getHandle() {
        return handle;
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
        private final MultiversionedHandle<Stack<E>> readyToPopStackHandle;
        private final MultiversionedHandle<Stack<E>> pushedStackHandle;
        private final MultiversionedHandle<Queue<E>> handle;
        private final int maxCapacity;

        DematerializedQueue(Queue<E> queue) {
            this.handle = queue.handle;
            this.readyToPopStackHandle = MultiversionedStmUtils.getHandle(queue.readyToPopStackRef, queue.readyToPopStack);
            this.pushedStackHandle = MultiversionedStmUtils.getHandle(queue.pushedStackRef, queue.pushedStack);
            this.maxCapacity = queue.maxCapacity;
        }

        @Override
        public MultiversionedHandle<Queue<E>> getHandle() {
            return handle;
        }

        @Override
        public Queue rematerialize(Transaction t) {
            return new Queue<E>(this, t);
        }
    }
}
