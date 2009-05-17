package org.multiverse.collections;

import static org.multiverse.api.StmUtils.retry;
import org.multiverse.api.TmEntity;
import org.multiverse.api.Unmanaged;

import java.util.ArrayList;
import java.util.List;

@TmEntity
public final class Stack<E> {

    @Unmanaged
    protected StackNode<E> head;
    protected int size;

    public Stack() {
    }

    public E peek() {
        return head == null ? null : head.value;
    }

    public boolean isEmpty() {
        return head == null;
    }

    public void push(E item) {
        if (item == null) throw new NullPointerException();
        head = new StackNode<E>(head, item);
        size++;
    }

    public E pop() {
        if (head == null)
            retry();

        return removeTopItem();
    }

    public E tryPop() {
        if (head == null)
            return null;

        return removeTopItem();
    }

    private E removeTopItem() {
        StackNode<E> oldHead = head;
        this.head = oldHead.next;
        size--;
        return oldHead.value;
    }

    public int size() {
        return size;
    }

    /**
     * Returns a List representation of the stack with the last pushed element as the head of the list.
     *
     * @return the List representation of stack
     */
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
        if (thatObj == this)
            return true;

        if (!(thatObj instanceof Stack))
            return false;

        Stack that = (Stack) thatObj;
        if (that.size != this.size)
            return false;

        if (this.head == null)
            return that.head == null;

        return this.asList().equals(that.asList());
    }

    @TmEntity
    public static final class StackNode<E> {

        @Unmanaged
        protected StackNode<E> next;
        protected E value;

        public StackNode(StackNode<E> next, E value) {
            this.next = next;
            this.value = value;
        }
    }
}
