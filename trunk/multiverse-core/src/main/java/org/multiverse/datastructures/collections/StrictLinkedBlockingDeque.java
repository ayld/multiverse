package org.multiverse.datastructures.collections;

import org.multiverse.api.annotations.AtomicMethod;
import org.multiverse.api.annotations.AtomicObject;
import org.multiverse.utils.TodoException;

import java.util.*;
import java.util.concurrent.BlockingDeque;

/**
 * A {@link BlockingDeque} and {@link List} implementation that used STM as concurrency
 * control mechanism.
 *
 * @param <E>
 */
@AtomicObject
public class StrictLinkedBlockingDeque<E> extends AbstractBlockingDeque<E>
        implements List<E> {
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

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        throw new TodoException();
    }

    @Override
    @AtomicMethod(readonly = true)
    public E get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }

        return getNode(index).value;
    }

    @Override
    public E set(int index, E element) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }

        if (element == null) {
            throw new NullPointerException();
        }

        Node<E> node = getNode(index);
        E old = node.value;
        node.value = element;
        return old;
    }

    @Override
    public void add(int index, E element) {
        throw new TodoException();
    }

    @Override
    public E remove(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }

        Node<E> node = getNode(index);
        remove(node);
        return node.value;
    }

    private Node<E> getNode(int index) {
        Node<E> node;
        if (index < size / 2) {
            node = head;
            for (int k = 0; k < index; k++) {
                node = node.next;
            }
        } else {
            node = tail;
            for (int k = size - 1; k > index; k--) {
                node = node.prev;
            }
        }

        return node;
    }

    @Override
    @AtomicMethod(readonly = true)
    public int indexOf(Object o) {
        if (o == null) {
            throw new NullPointerException();
        }

        int index = 0;
        for (Node node = head; node != null; node = node.next) {
            if (node.value.equals(o)) {
                return index;
            } else {
                index++;
            }
        }

        return -1;
    }

    @Override
    @AtomicMethod(readonly = true)
    public int lastIndexOf(Object o) {
        if (o == null) {
            throw new NullPointerException();
        }

        int index = size - 1;
        for (Node node = tail; node != null; node = node.prev) {
            if (node.value.equals(o)) {
                return index;
            } else {
                index--;
            }
        }

        return -1;
    }

    @Override
    public ListIterator<E> listIterator() {
        return listIterator(0);
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }

        throw new TodoException();
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        throw new TodoException();
    }

    @Override
    public boolean remove(Object item) {
        Node<E> found = findNode(item);

        if (found == null) {
            return false;
        } else {
            removeNode(found);
            return true;
        }
    }

    private void removeNode(Node<E> node) {
        size--;

        if (node == head) {
            head = node.next;
        }

        if (node == tail) {
            tail = node.prev;
        }

        if(node.next!=null){
            node.next.prev = node.prev;
        }

        if(node.prev!=null){
            node.prev.next = node.next;
        }
    }

    private Node<E> findNode(Object value) {
        Node<E> node = head;
        while (node != null) {
            if (node.value == null?value==null:node.value.equals(value)) {
                return node;
            } else {
                node = node.next;
            }
        }

        return null;
    }

    @AtomicObject
    private class IteratorImpl<E> implements Iterator<E> {
        private Node<E> nextNode;
        private Node<E> currentNode;

        private IteratorImpl(Node<E> nextNode) {
            this.currentNode = null;
            this.nextNode = nextNode;
        }

        @Override
        @AtomicMethod(readonly = true)
        public boolean hasNext() {
            return nextNode != null;
        }

        @Override
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            currentNode = nextNode;
            nextNode = nextNode.next;
            return currentNode.value;
        }

        @Override
        public void remove() {
            if(currentNode == null){
                throw new NoSuchElementException();
            }

            StrictLinkedBlockingDeque.this.removeNode((Node) currentNode);
        }
    }


    @AtomicObject
    public static class Node<E> {
        public E value;
        public Node<E> next;
        public Node<E> prev;

        public Node(E value) {
            this.value = value;
        }

    }
}
