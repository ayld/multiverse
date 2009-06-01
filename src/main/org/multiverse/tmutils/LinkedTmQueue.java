package org.multiverse.tmutils;

import static org.multiverse.api.StmUtils.retry;
import org.multiverse.api.annotations.NonEscaping;
import org.multiverse.api.annotations.TmEntity;

import static java.lang.String.format;
import java.util.Collections;
import static java.util.Collections.reverse;
import java.util.List;

/**
 * A {@link TmQueue} implementation that uses 2 stacks to allow concurrent takes and puts;
 * just like the {@link java.util.concurrent.LinkedBlockingQueue}.
 * <p/>
 * From a concurrency perspective it has some issues:
 * - doesn't allow concurrent takes
 * - doesn't allow concurrent puts
 * - starvation problems when flipping (so a taking transaction could starve because the flip
 * never succeeds) that increase because the amount of work to do the flip, increases.
 *
 * @author Peter Veentjer.
 * @param <E>
 */
@TmEntity
public final class LinkedTmQueue<E> implements TmQueue<E> {

    @NonEscaping
    private final TmStack<E> readyToPopStack;
    @NonEscaping
    private final TmStack<E> pushedStack;
    private final int maxCapacity;

    /**
     * Creates a new DoubleEndedTmQueue with a maximum capacity of Integer.MAX_VALUE.
     */
    public LinkedTmQueue() {
        this(Integer.MAX_VALUE);
    }

    /**
     * Creates a new DoubleEndedTmQueue with the provided maximumCapacity.
     *
     * @param maximumCapacity the maximum number of items in the queue.
     * @throws IllegalArgumentException if maximumCapacity  smaller than 0.
     */
    public LinkedTmQueue(int maximumCapacity) {
        if (maximumCapacity < 0) {
            throw new IllegalArgumentException();
        }
        this.maxCapacity = maximumCapacity;

        //moved into the constructor to bypass instrumentation issues; will be solved in the future.
        this.readyToPopStack = new LinkedTmStack<E>();
        this.pushedStack = new LinkedTmStack<E>();
    }

    @Override
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

    /**
     * Moves all items from the pushedStack to the readyToPopStack.
     */
    private void flip() {
        while (!pushedStack.isEmpty()) {
            E item = pushedStack.pop();
            readyToPopStack.push(item);
        }
    }

    @Override
    public void push(E value) {
        if (size() == maxCapacity) {
            retry();
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

    //@Override
    public List<E> asList() {
        if (isEmpty()) {
            return Collections.EMPTY_LIST;
        }

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

        if (!(thatObj instanceof LinkedTmQueue)) {
            return false;
        }

        LinkedTmQueue<E> that = (LinkedTmQueue<E>) thatObj;
        if (that.size() != this.size()) {
            return false;
        }

        List<E> thatList = that.asList();
        List<E> thisList = this.asList();
        return thatList.equals(thisList);
    }
}
