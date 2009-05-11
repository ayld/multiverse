package org.multiverse.multiversionedstm.examples;

import org.multiverse.api.LazyReference;
import org.multiverse.api.StmUtils;
import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.*;

import static java.lang.String.format;
import static java.util.Collections.reverse;
import java.util.List;

public final class ExampleQueue<E> implements MaterializedObject {

    private ExampleStack<E> readyToPopStack;
    private ExampleStack<E> pushedStack;
    private final int maxCapacity;

    public ExampleQueue() {
        this(Integer.MAX_VALUE);
    }

    public ExampleQueue(int maximumCapacity) {
        if (maximumCapacity < 1)
            throw new IllegalArgumentException();
        this.maxCapacity = maximumCapacity;

        //moved into the constructor.
        this.readyToPopStack = new ExampleStack<E>();
        this.pushedStack = new ExampleStack<E>();
        this.handle = new DefaultMultiversionedHandle<ExampleQueue<E>>();
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
            StmUtils.retry();

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

        if (!(thatObj instanceof ExampleQueue))
            return false;

        ExampleQueue<E> that = (ExampleQueue<E>) thatObj;
        if (that.size() != this.size())
            return false;

        List<E> thatList = that.asList();
        List<E> thisList = this.asList();
        return thatList.equals(thisList);
    }

    //================== generated =================

    private DematerializedQueue<E> lastDematerialized;
    private final MultiversionedHandle<ExampleQueue<E>> handle;
    private LazyReference<ExampleStack<E>> pushedStackRef;
    private LazyReference<ExampleStack<E>> readyToPopStackRef;

    public ExampleQueue(DematerializedQueue<E> dematerializedQueue, Transaction transaction) {
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
    public MultiversionedHandle<ExampleQueue<E>> getHandle() {
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
        private final MultiversionedHandle<ExampleStack<E>> readyToPopStackHandle;
        private final MultiversionedHandle<ExampleStack<E>> pushedStackHandle;
        private final MultiversionedHandle<ExampleQueue<E>> handle;
        private final int maxCapacity;

        DematerializedQueue(ExampleQueue<E> queue) {
            this.handle = queue.handle;
            this.readyToPopStackHandle = MultiversionedStmUtils.getHandle(queue.readyToPopStackRef, queue.readyToPopStack);
            this.pushedStackHandle = MultiversionedStmUtils.getHandle(queue.pushedStackRef, queue.pushedStack);
            this.maxCapacity = queue.maxCapacity;
        }

        @Override
        public MultiversionedHandle<ExampleQueue<E>> getHandle() {
            return handle;
        }

        @Override
        public ExampleQueue rematerialize(Transaction t) {
            return new ExampleQueue<E>(this, t);
        }
    }
}
