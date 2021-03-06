package org.multiverse.datastructures.collections;

import org.multiverse.api.annotations.AtomicMethod;
import org.multiverse.api.annotations.AtomicObject;
import org.multiverse.utils.TodoException;

import java.util.*;

/**
 * A general purposes collection structure that could be considered a work horse because it implements a lot of
 * interfaces: </ol> <li>{@link Iterable}</li> <li>{@link java.util.Collection}</li> <li>{@link java.util.List}</li>
 * <li>{@link java.util.Queue}</li> <li>{@link java.util.concurrent.BlockingQueue}</li> <li>{@link java.util.Deque}</li>
 * <li>{@link java.util.concurrent.BlockingDeque}</li> </ol>
 * <p/>
 * Each operation on this TransactionalLinkedList is atomic by default, and of course can participate in already running
 * transactions.
 * <p/>
 * There is a scalability issue with this structure and it has to do with unwanted writeconflicts. Although a take and
 * put can be executed concurrently because there is a seperate tail and head to place items on, one of the transactions
 * is going to fail because of a write conflict on the size field, or on the head/tail because of the object granularity
 * of the stm.  This is an issue that is going to be solved in the future, but for the moment this structure will not be
 * very concurrent. This even gets worse with longer transactions that are typical for stm's, compared to classic
 * concurrency (the synchronized block could be seen as a transaction).
 *
 * @author Peter Veentjer.
 * @param <E>
 */
@AtomicObject
public class TransactionalLinkedList<E> extends AbstractBlockingDeque<E> implements List<E> {

    private final int maxCapacity;

    //in the future the size needs to be changed using a 'commute' operation like
    //clojure provides to prevent unwanted writeconflicts.
    private int size;

    //in the future the head and tail need to be moved to a 'ref' so that they won't cause
    //write conflicts. This is solution for stm's with object granularity that cause
    //false writeconflicts.
    private Node<E> head;
    private Node<E> tail;

    public TransactionalLinkedList() {
        this(Integer.MAX_VALUE);
        size = 0;//needed to force a write, will be removed in the future
    }

    public TransactionalLinkedList(int maxCapacity) {
        if (maxCapacity < 0) {
            throw new IllegalArgumentException("maxCapacity can't be smaller than 0");
        }

        this.maxCapacity = maxCapacity;
        this.size = 0;//needed to force a write, will be removed in the future
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

        Node<E> newNode = new Node<E>(e);

        if (size == 0) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
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
        return new IteratorImpl();
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
    //@AtomicMethod(readonly = true)
    public Iterator<E> descendingIterator() {
        return new DescendingIteratorImpl();
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
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

        return new ListIteratorImpl(index, getNode(index));
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
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

        if (node.next != null) {
            node.next.prev = node.prev;
        }

        if (node.prev != null) {
            node.prev.next = node.next;
        }
    }

    private Node<E> findNode(Object value) {
        Node<E> node = head;
        while (node != null) {
            if (node.value == null ? value == null : node.value.equals(value)) {
                return node;
            } else {
                node = node.next;
            }
        }

        return null;
    }

    public int hashCode() {
        int hashCode = 1;
        Iterator<E> i = iterator();
        while (i.hasNext()) {
            E obj = i.next();
            hashCode = 31 * hashCode + (obj == null ? 0 : obj.hashCode());
        }
        return hashCode;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof List)) {
            return false;
        }

        ListIterator<E> e1 = listIterator();
        ListIterator e2 = ((List) o).listIterator();
        while (e1.hasNext() && e2.hasNext()) {
            E o1 = e1.next();
            Object o2 = e2.next();
            if (!(o1 == null ? o2 == null : o1.equals(o2))) {
                return false;
            }
        }
        return !(e1.hasNext() || e2.hasNext());
    }

    @AtomicObject
    public class ListIteratorImpl implements ListIterator<E> {

        private int index;
        private Node<E> node;

        public ListIteratorImpl(int index, Node<E> node) {
            this.index = index;
            this.node = node;
        }

        @Override
        public boolean hasNext() {
            return node.next != null;
        }

        @Override
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            E value = node.value;
            node = node.next;
            return value;
        }

        @Override
        public boolean hasPrevious() {
            return node.prev != null;
        }

        @Override
        public E previous() {
            if (!hasPrevious()) {
                throw new NoSuchElementException();
            }

            E value = node.value;
            node = node.prev;
            return value;
        }

        @Override
        public int nextIndex() {
            throw new TodoException();
        }

        @Override
        public int previousIndex() {
            throw new TodoException();
        }

        @Override
        public void remove() {
            if (node == null) {
                throw new NoSuchElementException();
            }

            TransactionalLinkedList.this.removeNode(node);
        }

        @Override
        public void set(E e) {
            throw new TodoException();
        }

        @Override
        public void add(E e) {
            throw new TodoException();
        }
    }

    @AtomicObject
    public class IteratorImpl implements Iterator<E> {

        private Node<E> next;
        private Node<E> current;

        private IteratorImpl() {
            this.current = null;
            this.next = head;
        }

        @Override
        @AtomicMethod(readonly = true)
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            current = next;
            next = next.next;
            return current.value;
        }

        @Override
        public void remove() {
            if (current == null) {
                throw new NoSuchElementException();
            }

            TransactionalLinkedList.this.removeNode(current);
        }
    }

    @AtomicObject
    public class DescendingIteratorImpl implements Iterator<E> {

        private Node<E> previous;
        private Node<E> current;

        private DescendingIteratorImpl() {
            this.current = null;
            this.previous = tail;
        }

        @Override
        @AtomicMethod(readonly = true)
        public boolean hasNext() {
            return previous != null;
        }

        @Override
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            current = previous;
            previous = previous.prev;
            return current.value;
        }

        @Override
        public void remove() {
            if (current == null) {
                throw new NoSuchElementException();
            }

            TransactionalLinkedList.this.removeNode(current);
        }
    }

    @AtomicObject
    public static class Node<E> {

        public Node<E> next;
        public Node<E> prev;
        public E value;

        public Node(E value) {
            this.value = value;
        }
    }
}
