package org.multiverse.tcollections;

import java.util.List;

/**
 * @param <E>
 */
public interface TQueue<E> {

    /**
     * Removes all items from the queue.
     */
    void clear();

    /**
     * Returns the maximum capacity of the queue. If the queue is unbound, Integer.MAX_VALUE
     * is returned.
     *
     * @return the maximum capacity of this queue.
     */
    int getMaxCapacity();

    /**
     * Tries to pop an item of the queue. If no item is availble, null is returned.
     *
     * @return the item popped, or null if no item is available.
     */
    E tryPop();

    /**
     * Pops an item of the queue, or do a retry.
     *
     * @return an item of the queue (value will never be null).
     */
    E pop();

    /**
     * Pushes an item on the queue. If the queue has reached its capacity, the queue does a retry.
     *
     * @param item the item to push.
     * @throws NullPointerException if item is null.
     */
    void push(E item);

    /**
     * Checks if the queue is empty.
     *
     * @return true if the queue is empty, false otherwise.
     */
    boolean isEmpty();

    /**
     * Returns the size of the queue.
     *
     * @return the size of the queue.
     */
    int size();

    /**
     * Returns the content of the queue as list. The content of the queue is not altered.
     *
     * @return the content of the queue as list.
     */
    List<E> asList();
}
