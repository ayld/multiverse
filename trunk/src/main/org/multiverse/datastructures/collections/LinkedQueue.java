package org.multiverse.datastructures.collections;

import static org.multiverse.api.StmUtils.retry;
import org.multiverse.api.annotations.AtomicObject;
import org.multiverse.utils.TodoException;

import java.util.AbstractCollection;
import java.util.Iterator;

/**
 * A Queue that uses 2 SingleLinkedStacks; one for the head and one for the tail. See the
 * {@link java.util.concurrent.LinkedBlockingQueue} for more information.
 *
 * @author Peter Veentjer
 */
@AtomicObject
public final class LinkedQueue<E> extends AbstractCollection<E> {

    private final SingleLinkedStack<E> pushedStack;
    private final SingleLinkedStack<E> readyToPopStack;
    private final int maxCapacity;

    /**
     * Creates a LinkedQueue with Integer.MAX_VALUE as maxCapacity.
     */
    public LinkedQueue() {
        pushedStack = new SingleLinkedStack<E>();
        readyToPopStack = new SingleLinkedStack<E>();
        maxCapacity = Integer.MAX_VALUE;
    }

    /**
     * Creates a LinkedQueue with the provided maxCapaciy.
     *
     * @param maxCapacity the maximumCapacity of this DoubleLinkedQueue.
     * @throws IllegalArgumentException if maxCapacity smaller than zero.
     */
    public LinkedQueue(int maxCapacity) {
        if (maxCapacity < 0) {
            throw new IllegalArgumentException();
        }
        pushedStack = new SingleLinkedStack<E>();
        readyToPopStack = new SingleLinkedStack<E>();
        this.maxCapacity = maxCapacity;
    }

    /**
     * Returns the maximum capacity of this LinkedQueue.
     *
     * @return the maximum capacity. The returned value will always be equal or larger than zero.
     */
    public int getMaxCapacity() {
        return maxCapacity;
    }

    @Override
    public void clear() {
        pushedStack.clear();
        readyToPopStack.clear();
    }

    /**
     * Pushes an item on this LinkedQueue. Executes a retry if the queue already has reached its
     * maximum capacity.
     *
     * @param item the item to push.
     * @throws NullPointerException if item is null
     */
    public void push(E item) {
        if (size() == maxCapacity) {
            retry();
        }

        pushedStack.push(item);
    }

    /**
     * Takes an item from this LinkedQueue. If no item is available, a retry is executed.
     *
     * @return the taken item
     */
    public E take() {
        if (readyToPopStack.isEmpty()) {
            while (!pushedStack.isEmpty()) {
                readyToPopStack.push(pushedStack.pop());
            }
        }

        return readyToPopStack.pop();
    }

    @Override
    public int size() {
        return pushedStack.size() + readyToPopStack.size();
    }

    @Override
    public boolean isEmpty() {
        return pushedStack.isEmpty() && readyToPopStack.isEmpty();
    }

    @Override
    public Iterator<E> iterator() {
        return new IteratorImpl<E>();
    }

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
