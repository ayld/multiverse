package org.multiverse.datastructures.collections;

import org.multiverse.api.annotations.AtomicMethod;
import org.multiverse.api.annotations.AtomicObject;
import org.multiverse.utils.TodoException;

import java.util.Iterator;
import java.util.concurrent.BlockingQueue;

/**
 * A strict FIFO {@link BlockingQueue} implementation that used STM as concurrency control mechanism. In the future
 * also less strict implementations will be added that have improved concurrency.
 * <p/>
 * Implementation details:
 * This queue uses 2 {@link BlockingStack}s:
 * <ol>
 * <li>stack for the head where items can be added</li>
 * <li>stack for the tail where items can be removed</li>
 * </ol>
 * The advantage of this approach is that removal and addition of items can execute concurrently (no write-conflicts).
 * Unless the tail stack is empty and items need to be moved from the head stack to the tail stack. This moving items from
 * one stack to another could cause a lot of livelocking and starvation, so the STM need to worry about that.
 * <p/>
 * See the {@link java.util.concurrent.LinkedBlockingQueue} for more information.
 *
 * @author Peter Veentjer
 */
@AtomicObject
public final class StrictLinkedBlockingQueue<E> extends AbstractBlockingQueue<E> {

    private final BlockingStack<E> pushedStack = new StrictSingleLinkedBlockingStack<E>();
    private final BlockingStack<E> readyToPopStack = new StrictSingleLinkedBlockingStack<E>();
    private final int maxCapacity;

    /**
     * Creates a LinkedQueue with Integer.MAX_VALUE as maxCapacity.
     */
    public StrictLinkedBlockingQueue() {
        maxCapacity = Integer.MAX_VALUE;
    }

    /**
     * Creates a LinkedQueue with the provided maxCapacity.
     *
     * @param maxCapacity the maximumCapacity of this DoubleLinkedQueue.
     * @throws IllegalArgumentException if maxCapacity smaller than zero.
     */
    public StrictLinkedBlockingQueue(int maxCapacity) {
        if (maxCapacity < 0) {
            throw new IllegalArgumentException();
        }
        this.maxCapacity = maxCapacity;
    }

    /**
     * Returns the maximum capacity of this LinkedQueue.
     *
     * @return the maximum capacity. The returned value will always be equal or larger than zero.
     */
    @AtomicMethod(readonly = true)
    public int getMaxCapacity() {
        return maxCapacity;
    }

    @Override
    public void clear() {
        pushedStack.clear();
        readyToPopStack.clear();
    }

    @Override
    protected E doRemove() {
        if (readyToPopStack.isEmpty()) {
            while (!pushedStack.isEmpty()) {
                readyToPopStack.push(pushedStack.pop());
            }
        }

        return readyToPopStack.pop();
    }

    @Override
    protected void doAdd(E item) {
        if (item == null) {
            throw new NullPointerException();
        }

        pushedStack.push(item);
    }

    @Override
    protected boolean isFull() {
        return size() == maxCapacity;
    }

    @Override
    @AtomicMethod(readonly = true)
    public int size() {
        return pushedStack.size() + readyToPopStack.size();
    }

    @Override
    @AtomicMethod(readonly = true)
    public boolean isEmpty() {
        return pushedStack.isEmpty() && readyToPopStack.isEmpty();
    }

    @Override
    //@AtomicMethod(readonly = true)
    public Iterator<E> iterator() {
        return new IteratorImpl<E>();
    }

    @Override
    @AtomicMethod(readonly = true)
    public int remainingCapacity() {
        return maxCapacity - size();
    }

    @Override
    @AtomicMethod(readonly = true)
    public E peek() {
        if (!pushedStack.isEmpty()) {
            return pushedStack.peek();
        }

        throw new TodoException();
    }

    @AtomicObject
    static class IteratorImpl<E> implements Iterator<E> {

        @Override
        public boolean hasNext() {
            throw new TodoException();
        }

        @Override
        public E next() {
            throw new TodoException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
