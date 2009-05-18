package org.multiverse.collections;

import org.multiverse.api.StmUtils;
import org.multiverse.api.annotations.TmEntity;
import org.multiverse.api.annotations.Unmanaged;

import static java.lang.String.format;
import static java.util.Collections.reverse;
import java.util.List;

@TmEntity
public final class Queue<E> {

    @Unmanaged
    private final Stack<E> readyToPopStack;
    @Unmanaged
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
            StmUtils.retry();

        pushedStack.push(value);
    }

    public boolean isEmpty() {
        return readyToPopStack.isEmpty() && pushedStack.isEmpty();
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
}
