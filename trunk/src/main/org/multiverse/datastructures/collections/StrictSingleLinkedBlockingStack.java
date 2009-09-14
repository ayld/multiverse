package org.multiverse.datastructures.collections;

import static org.multiverse.api.StmUtils.retry;
import org.multiverse.api.annotations.AtomicMethod;
import org.multiverse.api.annotations.AtomicObject;

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A {@link BlockingStack} implementation that uses a single linked list to store the elements.
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
     * Returns the maximum capacity of this Stack. The returned value will always be equal or larger
     * than zero.
     *
     * @return the maximum capacity of this Stack.
     */
    @AtomicMethod(readonly = true)
    public int getMaximumCapacity() {
        return maximumCapacity;
    }

    @AtomicMethod(readonly = true)
    public int size() {
        return size;
    }

    @AtomicMethod(readonly = true)
    public E peek() {
        return head == null ? null : head.value;
    }

    public E pop() {
        if (head == null) {
            retry();
        }

        size--;
        Node<E> oldHead = head;
        head = head.next;
        return oldHead.value;
    }

    public void push(E item) {
        if (item == null) {
            throw new NullPointerException();
        }

        if (size == maximumCapacity) {
            retry();
        }

        head = new Node<E>(head, item);
        size++;
    }

    public void clear() {
        size = 0;
        head = null;
    }

    @Override
    @AtomicMethod(readonly = true)
    public Iterator<E> iterator() {
        return new NodeIterator<E>(head);
    }

    static class NodeIterator<E> implements Iterator<E> {
        Node<E> current;

        NodeIterator(Node<E> current) {
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


