package org.multiverse.datastructures.collections;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * A BlockingStack interface similar to the {@link java.util.concurrent.BlockingQueue} and the
 * {@link java.util.concurrent.BlockingDeque} interface.
 * <p/>
 * The java.util.Stack class has no interface and there is no explicit BlockingStack interface (there is a
 * {@link java.util.concurrent.BlockingDeque} that can be used as a Stack interface. That is why one was created for the Multiverse
 * project so that different stack implementations can be used without changing (too much) code.
 * <p/>
 * <p>Memory consistency effects: As with other concurrent collections, actions in a thread prior to placing an object into a
 * {@code BlockingStack}
 * <a href="package-summary.html#MemoryVisibility"><i>happen-before</i></a> actions subsequent to the access or removal
 * of that element from the {@code BlockingStack} in another thread.
 *
 * @param <E>
 */
public interface BlockingStack<E> extends Collection<E> {

    /**
     * Returns the number of additional elements that this stack can ideally
     * (in the absence of memory or resource constraints) accept without
     * blocking, or <tt>Integer.MAX_VALUE</tt> if there is no intrinsic
     * limit.
     * <p/>
     * <p>Note that you <em>cannot</em> always tell if an attempt to insert
     * an element will succeed by inspecting <tt>remainingCapacity</tt>
     * because it may be the case that another thread is about to
     * insert or remove an element.
     *
     * @return the remaining capacity
     */
    int getRemainingCapacity();

    /**
     * Retrieves, but does not remove the last pushed item of this Stack.
     *
     * @return the the last pushed item of the stack, or null if the stack is empty.
     */
    E peek();

    /**
     * Pops an item of the stack. Waiting if necessary for an item to be placed.
     *
     * @return the popped item.
     */
    E pop();

    /**
     * Retrieves and removes most recently added item.
     *
     * @return the first element of this stack, or <tt>null</tt> if this stack is empty
     */
    E poll();

    /**
     * Pushes an item on the stack  or fails with an IllegalStateException if the item can't
     * be placed due to capacity restrictions.
     *
     * @param item the item to push.
     * @throws NullPointerException  if item is null.
     * @throws IllegalStateException if the element cannot be added at this
     *                               time stack to capacity restrictions
     */
    void push(E item);

    /**
     * Puts an item on the stack. Waiting if necessary for space to become available.
     *
     * @param item the item to add to this stack.
     * @throws NullPointerException if item is null.
     * @throws InterruptedException if the thread was interrupted during the execution
     */
    void put(E item) throws InterruptedException;

    /**
     * Offers an item to the stack.
     *
     * @param item the item to add to this stack
     * @return true if the item was added successfully, false otherwise.
     * @throws NullPointerException if item is null.
     */
    boolean offer(E item);

    boolean offer(E item, long timeout, TimeUnit unit) throws InterruptedException;

    /**
     * Removes all available elements from this stack and adds them to the given collection.  This operation may be more
     * efficient than repeatedly polling this stack.  A failure encountered while attempting to add elements to
     * collection <tt>c</tt> may result in elements being in neither, either or both collections when the associated exception is
     * thrown.  Attempts to drain a stack to itself result in <tt>IllegalArgumentException</tt>.
     *
     * @param c the collection to transfer elements into
     * @return the number of elements transferred
     * @throws UnsupportedOperationException if addition of elements
     *                                       is not supported by the specified collection
     * @throws ClassCastException            if the class of an element of this stack
     *                                       prevents it from being added to the specified collection
     * @throws NullPointerException          if the specified collection is null
     * @throws IllegalArgumentException      if the specified collection is this
     *                                       stack, or some property of an element of this stack prevents
     *                                       it from being added to the specified collection
     */
    int drainTo(Collection<? super E> c);

    /**
     * Removes at most the given number of available elements from this stack and adds them to the given collection.  A failure
     * encountered while attempting to add elements to collection <tt>c</tt> may result in elements being in neither,
     * either or both collections when the associated exception is thrown.  Attempts to stack a stack to itself result in
     * <tt>IllegalArgumentException</tt>.
     *
     * @param c           the collection to transfer elements into
     * @param maxElements the maximum number of elements to transfer
     * @return the number of elements transferred
     * @throws UnsupportedOperationException if addition of elements
     *                                       is not supported by the specified collection
     * @throws ClassCastException            if the class of an element of this stack
     *                                       prevents it from being added to the specified collection
     * @throws NullPointerException          if the specified collection is null
     * @throws IllegalArgumentException      if the specified collection is this
     *                                       stack, or some property of an element of this stack prevents
     *                                       it from being added to the specified collection
     */
    int drainTo(Collection<? super E> c, int maxElements);
}
