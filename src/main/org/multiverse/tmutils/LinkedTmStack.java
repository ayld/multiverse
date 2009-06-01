package org.multiverse.tmutils;

import static org.multiverse.api.StmUtils.retry;
import org.multiverse.api.annotations.NonEscaping;
import org.multiverse.api.annotations.TmEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Standard {@link TmStack} implementation that uses a single linked list to store the
 * content.
 *
 * @param <E>
 */
@TmEntity
public final class LinkedTmStack<E> implements TmStack<E> {

    @NonEscaping
    private StackNode<E> head;
    private int size;

    @Override
    public E peek() {
        return head == null ? null : head.value;
    }

    @Override
    public void clear() {
        size = 0;
        head = null;
    }

    @Override
    public boolean isEmpty() {
        return head == null;
    }

    @Override
    public void push(E item) {
        if (item == null) {
            throw new NullPointerException();
        }

        head = new StackNode<E>(head, item);
        size++;
    }

    @Override
    public E pop() {
        if (head == null) {
            retry();
        }

        return removeTopItem();
    }

    @Override
    public E tryPop() {
        if (head == null) {
            return null;
        }

        return removeTopItem();
    }

    private E removeTopItem() {
        StackNode<E> oldHead = head;
        this.head = oldHead.next;
        size--;
        return oldHead.value;
    }

    @Override
    public int size() {
        return size;
    }

    /**
     * Returns a List representation of the stack with the last pushed element as the head of the list.
     *
     * @return the List representation of stack
     */
    @Override
    public List<E> asList() {
        List<E> result = new ArrayList<E>(size());
        StackNode<E> node = head;
        for (int k = 0; k < size(); k++) {
            result.add(node.value);
            node = node.next;
        }
        return result;
    }

    @Override
    public String toString() {
        return asList().toString();
    }

    @Override
    public int hashCode() {
        return asList().hashCode();
    }

    @Override
    public boolean equals(Object thatObj) {
        if (thatObj == this) {
            return true;
        }

        if (!(thatObj instanceof LinkedTmStack)) {
            return false;
        }

        LinkedTmStack that = (LinkedTmStack) thatObj;
        if (that.size != this.size) {
            return false;
        }

        if (this.head == null) {
            return that.head == null;
        }

        return this.asList().equals(that.asList());
    }

    @TmEntity
    private static final class StackNode<E> {

        @NonEscaping
        private final StackNode<E> next;
        private final E value;

        public StackNode(StackNode<E> next, E value) {
            this.next = next;
            this.value = value;
        }
    }
}
