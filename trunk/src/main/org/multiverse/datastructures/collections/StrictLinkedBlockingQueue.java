package org.multiverse.datastructures.collections;

import static org.multiverse.api.StmUtils.retry;
import org.multiverse.api.annotations.AtomicMethod;
import org.multiverse.api.annotations.AtomicObject;
import org.multiverse.utils.TodoException;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * A strict FILO {@link BlockingQueue} implementation that used STM as concurrency control mechanism.
 * <p/>
 * Implementation details:
 * This queue uses 2 {@link BlockingStack}s:
 * <ol>
 * <li>stack for the head where items can be added</li>
 * <li>stack for the tail where items can be removed</li>
 * </ol>
 * The advantage of this approach is that removal and addition of items can execute concurrently (no write-conflicts).
 * Unless the tail stack is empty and items need to be moved from the head stack to the tail stack.
 * <p/>
 * See the {@link java.util.concurrent.LinkedBlockingQueue} for more information.
 *
 * @author Peter Veentjer
 */
@AtomicObject
public final class StrictLinkedBlockingQueue<E> extends AbstractCollection<E> implements BlockingQueue<E> {

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

    /**
     * Pushes an item on this LinkedQueue. Executes a retry if the queue already has reached its
     * maximum capacity.
     *
     * @param item the item to push.
     * @throws NullPointerException if item is null
     */
    public void put(E item) {
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
    @AtomicMethod(readonly = true)
    public Iterator<E> iterator() {
        return new IteratorImpl<E>();
    }

    @Override
    public boolean offer(E e) {
        if (remainingCapacity() == 0) {
            return false;
        }

        pushedStack.push(e);
        return true;
    }

    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
        throw new TodoException();
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        throw new TodoException();
    }

    @Override
    @AtomicMethod(readonly = true)
    public int remainingCapacity() {
        return maxCapacity - size();
    }

    @Override
    public int drainTo(Collection<? super E> c) {
        throw new TodoException();
    }

    @Override
    public int drainTo(Collection<? super E> c, int maxElements) {
        throw new TodoException();
    }

    @Override
    public E remove() {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return poll();
    }

    @Override
    public E poll() {
        throw new TodoException();
    }

    @Override
    public E element() {
        throw new TodoException();
    }

    @Override
    @AtomicMethod(readonly = true)
    public E peek() {
        throw new TodoException();
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
