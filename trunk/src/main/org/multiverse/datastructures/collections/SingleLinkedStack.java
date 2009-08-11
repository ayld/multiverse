package org.multiverse.datastructures.collections;

import static org.multiverse.api.StmUtils.retry;
import org.multiverse.api.annotations.AtomicObject;

/**
 * @author Peter Veentjer
 */
@AtomicObject
public class SingleLinkedStack<E> {

    private final int maximumCapacity;
    private int size;
    private Node<E> head;

    public SingleLinkedStack() {
        maximumCapacity = Integer.MAX_VALUE;
    }

    public SingleLinkedStack(int maxCapacity) {
        if (maxCapacity < 0) {
            throw new IllegalArgumentException();
        }
        this.maximumCapacity = maxCapacity;
    }

    public int getMaximumCapacity() {
        return maximumCapacity;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

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
    public String toString() {
        if (size == 0) {
            return "[]";
        }

        StringBuffer sb = new StringBuffer();
        sb.append("[");
        sb.append(head.value);
        Node<E> node = head.next;
        while (node != null) {
            sb.append(",");                        
            sb.append(node.value);
            node = node.next;
        }
        sb.append("]");
        return sb.toString();
    }

    private static class Node<E> {
        final Node<E> next;
        final E value;

        private Node(Node<E> next, E value) {
            this.next = next;
            this.value = value;
        }
    }
}


