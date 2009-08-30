package org.multiverse.datastructures.collections;

import org.multiverse.api.annotations.AtomicObject;

/**
 * A Queue that used 2 single linked lists. See the {@link org.multiverse.datastructures.collections.DoubleLinkedQueue}
 * for more information.
 *
 * @author Peter Veentjer
 */
@AtomicObject
public class DoubleLinkedQueue<E> {

    private final SingleLinkedStack<E> pushedStack;
    private final SingleLinkedStack<E> readyToPopStack;
    private final int maxCapacity;

    public DoubleLinkedQueue() {
        pushedStack = new SingleLinkedStack<E>();
        readyToPopStack = new SingleLinkedStack<E>();
        maxCapacity = Integer.MAX_VALUE;
    }

    public DoubleLinkedQueue(int maxCapacity) {
        if (maxCapacity < 0) {
            throw new IllegalArgumentException();
        }
        pushedStack = new SingleLinkedStack<E>();
        readyToPopStack = new SingleLinkedStack<E>();
        this.maxCapacity = maxCapacity;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void clear() {
        pushedStack.clear();
        readyToPopStack.clear();
    }

    public void push(E item) {
        if (size() > maxCapacity) {
            throw new IllegalStateException();
        }

        pushedStack.push(item);
    }

    public E take() {
        if (readyToPopStack.isEmpty()) {
            while (!pushedStack.isEmpty()) {
                readyToPopStack.push(pushedStack.pop());
            }
        }

        return readyToPopStack.pop();
    }

    public int size() {
        return pushedStack.size() + readyToPopStack.size();
    }

    public boolean isEmpty() {
        return pushedStack.isEmpty() && readyToPopStack.isEmpty();
    }
}
