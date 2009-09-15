package org.multiverse.datastructures.collections;

import org.multiverse.api.annotations.AtomicMethod;
import org.multiverse.api.annotations.AtomicObject;
import org.multiverse.utils.TodoException;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

/**
 * A strict LIFO {@link BlockingStack} implementation that uses a single linked list to store the elements.
 * <p/>
 * A SingleLinkedStack is not very concurrency friendly since there will be a lot of contention on the head.
 *
 * @author Peter Veentjer
 */
@AtomicObject
public final class StrictSingleLinkedBlockingStack<E> extends AbstractCollection<E> implements BlockingStack<E> {

    private final int maximumCapacity;
    private int size;
    private Node<E> head;

    /**
     * Creates a new SingleLinkedList with Integer.MAX_VALUE as capacity.
     */
    public StrictSingleLinkedBlockingStack() {
        maximumCapacity = Integer.MAX_VALUE;
    }

    /**
     * Creates a new bounded SingleLinkedStack
     *
     * @param maxCapacity the maximum capacity of this stack
     * @throws IllegalArgumentException if maxCapacity is smaller than zero.
     */
    public StrictSingleLinkedBlockingStack(int maxCapacity) {
        if (maxCapacity < 0) {
            throw new IllegalArgumentException();
        }
        this.maximumCapacity = maxCapacity;
    }

    /**
     * \
     *
     * @return
     */
    @AtomicMethod(readonly = true)
    public int getMaximumCapacity() {
        return maximumCapacity;
    }

    @Override
    //@AtomicMethod(readonly = true
    public Iterator<E> iterator() {
        return new IteratorImpl<E>(head);
    }

    @Override
    @AtomicMethod(readonly = true)
    public int size() {
        return size;
    }

    @Override
    public int getRemainingCapacity() {
        throw new TodoException();
    }

    @Override
    @AtomicMethod(readonly = true)
    public E peek() {
        return head == null ? null : head.value;
    }

    @Override
    public E pop() {
        throw new TodoException();
    }

    @Override
    public E poll() {
        throw new TodoException();
    }

    @Override
    public void push(E item) {
        if (item == null) {
            throw new NullPointerException();
        }
        throw new TodoException();
    }

    @Override
    public void put(E item) throws InterruptedException {
        if (item == null) {
            throw new NullPointerException();
        }
        throw new TodoException();
    }

    @Override
    public boolean offer(E item) {
        if (item == null) {
            throw new NullPointerException();
        }
        throw new TodoException();
    }

    @Override
    public boolean offer(E item, long timeout, TimeUnit unit) throws InterruptedException {
        throw new TodoException();
    }

    @Override
    public int drainTo(Collection<? super E> c) {
        int drained = size;
        for (E item : this) {
            c.add(item);
        }

        clear();
        return drained;
    }

    @Override
    public int drainTo(Collection<? super E> c, int maxElements) {
        throw new TodoException();
    }

    @AtomicObject
    static final class IteratorImpl<E> implements Iterator<E> {
        Node<E> current;

        IteratorImpl(Node<E> current) {
            this.current = current;
        }

        @Override
        public boolean hasNext() {
            return current != null;
        }

        @Override
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            Node<E> old = current;
            current = current.next;
            return old.value;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    @AtomicMethod(readonly = true)
    public boolean equals(Object thatObj) {
        if (thatObj == this) {
            return true;
        }

        if (!(thatObj instanceof StrictSingleLinkedBlockingStack)) {
            return false;
        }

        StrictSingleLinkedBlockingStack that = (StrictSingleLinkedBlockingStack) thatObj;
        if (that.size != this.size) {
            return false;
        }

        if (this.head == null) {
            return true;
        }

        return this.head.equals(that.head);
    }

    @Override
    @AtomicMethod(readonly = true)
    public int hashCode() {
        return head == null ? 0 : head.hashCode();
    }

    private static class Node<E> {
        final Node<E> next;
        final E value;

        private Node(Node<E> next, E value) {
            this.next = next;
            this.value = value;
        }

        @Override
        public int hashCode() {
            int hashCode = value.hashCode();
            Node<E> node = next;
            while (node != null) {
                hashCode = hashCode * 31 + node.value.hashCode();
                node = node.next;
            }
            return hashCode;
        }

        @Override
        public boolean equals(Object thatObj) {
            if (thatObj == this) {
                return true;
            }

            if ((thatObj instanceof Node)) {
                return false;
            }

            Node that = (Node) thatObj;
            if (!that.value.equals(this.value)) {
                return false;
            }

            if (that.next == null) {
                return true;
            }

            //todo: call should be recursive instead of iterative
            return that.next.equals(this.next);
        }
    }
}


