package org.multiverse.datastructures.collections;

import org.multiverse.api.annotations.AtomicMethod;
import org.multiverse.api.annotations.AtomicObject;
import org.multiverse.utils.TodoException;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingDeque;

/**
 * A {@link BlockingDeque} implementation that used STM as concurrency control mechanism.
 *
 * @param <E>
 */
@AtomicObject
public class StrictLinkedBlockingDeque<E> extends AbstractBlockingDeque<E> {
    private final int maxCapacity;

    private int size;
    private Node<E> head;
    private Node<E> tail;

    public StrictLinkedBlockingDeque() {
        this.maxCapacity = Integer.MAX_VALUE;
    }

    public StrictLinkedBlockingDeque(int maxCapacity) {
        if (maxCapacity < 0) {
            throw new IllegalArgumentException();
        }

        this.maxCapacity = maxCapacity;
    }

    @Override
    public void clear() {
        size = 0;
        head = null;
        tail = null;
    }

    @AtomicMethod(readonly = true)
    public int getMaxCapacity() {
        return maxCapacity;
    }

    @Override
    protected void doAddLast(E e) {
        if (e == null) {
            throw new NullPointerException();
        }

        Node<E> node = new Node<E>(e);
        if (size == 0) {
            head = node;
            tail = node;
        } else {
            tail.next = node;
            node.prev = tail;
            tail = node;
        }

        size++;
    }

    @Override
    protected void doAddFirst(E e) {
        if (e == null) {
            throw new NullPointerException();
        }

        Node<E> node = new Node<E>(e);
        if (size == 0) {
            head = node;
            tail = node;
        } else {
            head.prev = node;
            node.next = head;
            head = node;
        }

        size++;
    }

    @Override
    protected E doRemoveFirst() {
        E value = head.value;
        if (size == 1) {
            head = null;
            tail = null;
        } else {
            head.next.prev = null;
            head = head.next;
        }

        size--;
        return value;
    }

    @Override
    protected E doRemoveLast() {
        E value = tail.value;
        if (size == 1) {
            head = null;
            tail = null;
        } else {
            tail.prev.next = null;
            tail = tail.prev;
        }

        size--;
        return value;
    }

    @Override
    @AtomicMethod(readonly = true)
    public int size() {
        return size;
    }

    @Override
    //@AtomicMethod(readonly = true)
    public Iterator<E> iterator() {
        return new IteratorImpl<E>(head);
    }

    @Override
    @AtomicMethod(readonly = true)
    public int remainingCapacity() {
        return maxCapacity - size;
    }

    @Override
    @AtomicMethod(readonly = true)
    public E peekFirst() {
        return head == null ? null : head.value;
    }

    @Override
    @AtomicMethod(readonly = true)
    public E peekLast() {
        return tail == null ? null : tail.value;
    }

    @Override
    @AtomicMethod(readonly = true)
    public Iterator<E> descendingIterator() {
        throw new TodoException();
    }

    @AtomicObject
    static class Node<E> {
        final E value;
        Node<E> next;
        Node<E> prev;

        Node(E value) {
            this.value = value;
        }
    }

    @AtomicObject
    private static class IteratorImpl<E> implements Iterator<E> {
        private Node<E> head;

        private IteratorImpl(Node<E> head) {
            this.head = head;
        }

        @Override
        @AtomicMethod(readonly = true)
        public boolean hasNext() {
            return head != null;
        }

        @Override
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            E value = head.value;
            head = head.next;
            return value;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
