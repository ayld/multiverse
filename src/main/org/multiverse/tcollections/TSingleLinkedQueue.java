package org.multiverse.tcollections;

import org.multiverse.api.StmUtils;
import org.multiverse.api.annotations.NonEscaping;
import org.multiverse.api.annotations.TmEntity;

import static java.lang.String.format;
import static java.util.Collections.reverse;
import java.util.List;

@TmEntity
public final class TSingleLinkedQueue<E> implements TQueue<E> {

    @NonEscaping
    private final TSingleLinkedStack<E> readyToPopStack;
    @NonEscaping
    private final TSingleLinkedStack<E> pushedStack;
    private final int maxCapacity;

    public TSingleLinkedQueue() {
        this(Integer.MAX_VALUE);
    }

    public TSingleLinkedQueue(int maximumCapacity) {
        if (maximumCapacity < 1) {
            throw new IllegalArgumentException();
        }
        this.maxCapacity = maximumCapacity;

        //moved into the constructor.
        this.readyToPopStack = new TSingleLinkedStack<E>();
        this.pushedStack = new TSingleLinkedStack<E>();
    }


    public void clear() {
        readyToPopStack.clear();
        pushedStack.clear();
    }

    @Override
    public int getMaxCapacity() {
        return maxCapacity;
    }

    @Override
    public E tryPop() {
        E result = readyToPopStack.tryPop();
        if (result != null) {
            return result;
        }

        flip();
        return readyToPopStack.tryPop();
    }

    @Override
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

    @Override
    public void push(E value) {
        if (size() == maxCapacity) {
            StmUtils.retry();
        }

        pushedStack.push(value);
    }

    @Override
    public boolean isEmpty() {
        return readyToPopStack.isEmpty() && pushedStack.isEmpty();
    }

    @Override
    public int size() {
        return readyToPopStack.size() + pushedStack.size();
    }

    @Override
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
        if (thatObj == this) {
            return true;
        }

        if (!(thatObj instanceof TSingleLinkedQueue)) {
            return false;
        }

        TQueue<E> that = (TQueue<E>) thatObj;
        if (that.size() != this.size())
            return false;

        List<E> thatList = that.asList();
        List<E> thisList = this.asList();
        return thatList.equals(thisList);
    }
}
