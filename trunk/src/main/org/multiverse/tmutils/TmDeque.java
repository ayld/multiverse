package org.multiverse.tmutils;

import org.multiverse.api.annotations.TmEntity;

import java.util.Deque;

/**
 * The Transactional Memory version of the {@link BlockingDeque}. All moethods with timeouts
 * are removed. All interrupted exceptions are removed.
 * <p/>
 * todo: comment about waiting needs to be removed.
 *
 * @author Peter Veentjer.
 * @param <E>
 */
@TmEntity
public interface TmDeque<E> extends Deque<E> {

    /**
     * Inserts the specified element at the front of this deque,
     * waiting if necessary for space to become available.
     *
     * @param e the element to add
     * @throws ClassCastException       if the class of the specified element
     *                                  prevents it from being added to this deque
     * @throws NullPointerException     if the specified element is null
     * @throws IllegalArgumentException if some property of the specified
     *                                  element prevents it from being added to this deque
     */
    void putFirst(E e);

    /**
     * Inserts the specified element at the end of this deque,
     * waiting if necessary for space to become available.
     *
     * @param e the element to add
     * @throws ClassCastException       if the class of the specified element
     *                                  prevents it from being added to this deque
     * @throws NullPointerException     if the specified element is null
     * @throws IllegalArgumentException if some property of the specified
     *                                  element prevents it from being added to this deque
     */
    void putLast(E e);

    /**
     * Retrieves and removes the first element of this deque, waiting
     * if necessary until an element becomes available.
     *
     * @return the head of this deque
     */
    E takeFirst();

    /**
     * Retrieves and removes the last element of this deque, waiting
     * if necessary until an element becomes available.
     *
     * @return the tail of this deque
     */
    E takeLast();

    /**
     * Inserts the specified element into the queue represented by this deque
     * (in other words, at the tail of this deque), waiting if necessary for
     * space to become available.
     * <p/>
     * <p>This method is equivalent to {@link #putLast(Object) putLast}.
     *
     * @param e the element to add
     * @throws ClassCastException       if the class of the specified element
     *                                  prevents it from being added to this deque
     * @throws NullPointerException     if the specified element is null
     * @throws IllegalArgumentException if some property of the specified
     *                                  element prevents it from being added to this deque
     */
    void put(E e);

    /**
     * Retrieves and removes the head of the queue represented by this deque
     * (in other words, the first element of this deque), waiting if
     * necessary until an element becomes available.
     * <p/>
     * <p>This method is equivalent to {@link #takeFirst() takeFirst}.
     *
     * @return the head of this deque
     */
    E take();
}
