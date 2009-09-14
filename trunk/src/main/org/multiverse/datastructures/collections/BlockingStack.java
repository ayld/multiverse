package org.multiverse.datastructures.collections;

import java.util.Collection;

/**
 * A BlockingStack interface similar to the {@link java.util.concurrent.BlockingQueue} and the {@link java.util.concurrent.BlockingDeque}
 * interface.
 * <p/>
 * The java.util.Stack class has no interface and there is no explicit BlockingStack interface (there is a
 * {@link java.util.concurrent.BlockingDeque} that can be used as a Stack interface. That is why one was created for the Multiverse
 * project so that different stack implementations can be used without changing (too much) code.
 *
 * @param <E>
 */
public interface BlockingStack<E> extends Collection<E> {

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
     * Pushes an item on the stack. Waiting if necessary for space to become available.
     *
     * @param item the item to push.
     * @throws NullPointerException if item is null.
     */
    void push(E item);
}
