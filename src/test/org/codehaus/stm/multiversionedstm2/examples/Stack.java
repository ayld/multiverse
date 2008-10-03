package org.codehaus.stm.multiversionedstm2.examples;

import static org.codehaus.stm.TransactionMethods.retry;
import org.codehaus.stm.multiversionedstm2.Citizen;
import org.codehaus.stm.multiversionedstm2.DehydratedCitizen;
import org.codehaus.stm.multiversionedstm2.MultiversionedStm;

public class Stack<E> implements Citizen {

    private Node<E> head;

    public void push(E item) {
        if (item == null) throw new NullPointerException();
        head = new Node(item, head);
    }

    public E peek() {
        if (head == null)
            return null;

        return removeTopItem();
    }

    public E pop() {
        if (head == null)
            retry();

        return removeTopItem();
    }

    private E removeTopItem() {
        Node<E> oldHead = head;
        head = head.parent;
        return oldHead.value;
    }

    public int size() {
        return head == null ? 0 : head.size();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public static class Node<E> {
        final E value;
        final Node parent;

        Node(E value, Node prev) {
            this.value = value;
            this.parent = prev;
        }

        int size() {
            if (parent == null)
                return 1;
            else
                return parent.size() + 1;
        }
    }

    //============= generated =================

    private DehydratedStack initial;

    public boolean isDirty() {
        if (initial == null)
            return true;

        return initial.head != head;
    }

    public DehydratedStack dehydrate() {
        return isDirty() ? new DehydratedStack(this) : initial;
    }

    public DehydratedStack getInitial() {
        return initial;
    }

    static class DehydratedStack extends DehydratedCitizen {

        final Node head;

        public DehydratedStack(Stack stack) {
            head = stack.head;
        }

        public Citizen hydrate(MultiversionedStm.MultiversionedTransaction transaction) {
            Stack stack = new Stack();
            stack.initial = this;
            stack.head = head;
            return stack;
        }

        public long getVersion() {
            throw new RuntimeException();
        }
    }
}
