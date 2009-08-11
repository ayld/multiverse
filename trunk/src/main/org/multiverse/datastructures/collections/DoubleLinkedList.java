package org.multiverse.datastructures.collections;

import static org.multiverse.api.StmUtils.retry;
import org.multiverse.api.annotations.AtomicObject;
import org.multiverse.utils.TodoException;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A Double linked list.
 * <p/>
 * Also functions as a deque.
 * <p/>
 * Also functions as a queue
 * <p/>
 * Also functions as a stack.
 *
 * @author Peter Veentjer
 */
@AtomicObject
public class DoubleLinkedList<E> implements Iterable<E> {
    private int size;
    private LinkedNode<E> head;
    private LinkedNode<E> tail;
    private final int maxCapacity;

    public DoubleLinkedList() {
        this.maxCapacity = Integer.MAX_VALUE;
    }

    public DoubleLinkedList(int maxCapacity) {
        if (maxCapacity < 0) {
            throw new IllegalArgumentException();
        }

        this.maxCapacity = maxCapacity;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }

    public int firstIndexOf(E item) {
        LinkedNode<E> node = head;
        int index = 0;
        while (node != null) {
            if (node.value == item) {
                return index;
            }
            index++;
            node = node.next;
        }

        return -1;
    }

    public void addInFront(E item) {
        if (maxCapacity == size) {
            retry();
        }

        LinkedNode<E> node = new LinkedNode<E>(item);
        if (size == 0) {
            head = node;
            tail = node;
        } else {
            node.next = head;
            head.prev = node;
            head = node;
        }

        size++;
    }

    public void addAll(Iterable<E> newWorkers) {
        for(E item: newWorkers){
            add(item);
        }
    }

    public void add(E item) {
        if (maxCapacity == size) {
            retry();
        }

        LinkedNode<E> node = new LinkedNode<E>(item);
        if (size == 0) {
            head = node;
            tail = node;
        } else {
            node.prev = tail;
            tail.next = node;
            tail = node;
        }

        size++;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public E removeFirst() {
        if (size == 0) {
            throw new NoSuchElementException();
        }

        if (size == 1) {
            E result = head.value;
            head = null;
            tail = null;
            size = 0;
            return result;
        } else {
            LinkedNode<E> oldHead = head;
            LinkedNode<E> next = head.next;
            next.prev = null;
            head = next;
            size--;
            return oldHead.value;
        }
    }

    public E removeLast() {
        if (size == 0) {
            throw new NoSuchElementException();
        }

        if (size == 1) {
            E result = head.value;
            head = null;
            tail = null;
            size = 0;
            return result;
        } else {
            LinkedNode<E> oldTail = tail;
            LinkedNode<E> previous = tail.prev;
            previous.next = null;
            tail = previous;
            size--;
            return oldTail.value;
        }
    }

    public E takeFirst() {
        if (isEmpty()) {
            retry();
        }

        return removeFirst();
    }

    public E takeLast() {
        if (isEmpty()) {
            retry();
        }

        return removeFirst();
    }

    public E remove(int index) {
        if (index < 0 || index >= size) {
            throw new IllegalArgumentException();
        }

        throw new TodoException();
    }

    public boolean remove(E item) {
        //slow implementation
        int indexOf = firstIndexOf(item);
        if (indexOf == -1) {
            return false;
        } else {
            remove(indexOf);
            return true;
        }
    }

    public E get(int index) {
        if (index < 0 || index >= size) {
            throw new IllegalArgumentException();
        }

        //todo: if you are closer to the end, start searching from the end and not from the beginning.
        LinkedNode<E> result = head;
        for (int k = 0; k < index; k++) {
            result = result.next;
        }

        return result.value;
    }

    public Iterator<E> iterator() {
        return new IteratorImpl<E>(head);
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "[]";
        }

        LinkedNode node = head;
        StringBuffer sb = new StringBuffer();
        sb.append('[');
        sb.append(node.value);
        node = node.next;
        while (node != null) {
            sb.append(',');
            sb.append(node.value);
            node = node.next;
        }
        sb.append(']');
        return sb.toString();
    }

    @AtomicObject
    public static final class LinkedNode<E> {
        private LinkedNode<E> next;
        private LinkedNode<E> prev;
        private final E value;

        public LinkedNode(E value) {
            this.value = value;
            this.next = null;
            this.prev = null;
        }
    }

    @AtomicObject
    public static class IteratorImpl<E> implements Iterator<E> {

        private LinkedNode<E> node;

        public IteratorImpl(LinkedNode<E> first) {
            node = first;
        }

        @Override
        public boolean hasNext() {
            return node != null;
        }

        @Override
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            LinkedNode<E> old = node;
            node = old.next;
            return old.value;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}


