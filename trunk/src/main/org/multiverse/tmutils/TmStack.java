package org.multiverse.tmutils;

import org.multiverse.api.annotations.TmEntity;

import java.util.List;

/**
 * A Multiverse stack. A new interface is introduced because the jdk only has a stack implementation
 * (java.util.Stack) and no interface. And because we also need to provide 'blocking' version of
 * methods that call the rety/orelse functionality).
 *
 * @author Peter Veentjer.
 * @param <E>
 */
@TmEntity
public interface TmStack<E> {

    /**
     * Clears the stack.
     */
    void clear();

    /**
     * Checks if the MStack is empty.
     *
     * @return true if empty, false otherwise.
     */
    boolean isEmpty();

    /**
     * Pushes an item on the stack.
     *
     * @param item the item to push
     * @throws NullPointerException if item is null.
     */
    void push(E item);

    /**
     * Returns the top item without popping. If no item is on the stack, null is returned.
     *
     * @return the top item of the stack.
     */
    E peek();

    /**
     * Pops the top item, or do a retry when no item is available.
     *
     * @return the top item.
     */
    E pop();

    /**
     * Pops the top item is available, or null if none is found.
     *
     * @return the top item or null if no top item is found.
     */
    E tryPop();

    /**
     * Returns the current number of items in the stack.
     *
     * @return
     */
    int size();

    /**
     * Returns the content of the stack as list. This call doesn't change
     * the content of the stack.
     *
     * @return the content of the stack as list.
     */
    List<E> asList();
}
