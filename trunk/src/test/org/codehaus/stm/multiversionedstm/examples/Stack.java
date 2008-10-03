package org.codehaus.stm.multiversionedstm.examples;

import static org.codehaus.stm.TransactionMethods.retry;
import org.codehaus.stm.multiversionedstm.Citizen;
import org.codehaus.stm.multiversionedstm.HydratedCitizen;
import org.codehaus.stm.multiversionedstm.MultiversionedStm;
import org.codehaus.stm.util.EmptyIterator;

import java.util.Iterator;

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

    //================== generated dowdown ======================

    //generated
    private Node head_initial;
    private long ptr;
    private MultiversionedStm.MultiversionedTransaction transaction;

    public Iterator<Citizen> ___findNewlyborns() {
        return EmptyIterator.INSTANCE;
    }

    public void ___onAttach(MultiversionedStm.MultiversionedTransaction transaction) {
        this.transaction = transaction;
    }

    public MultiversionedStm.MultiversionedTransaction ___getTransaction() {
        return transaction;
    }

    public long ___getPointer() {
        return ptr;
    }

    public void ___setPointer(long ptr) {
        this.ptr = ptr;
    }

    public HydratedStack ___hydrate() {
        return new HydratedStack(head);
    }

    public boolean ___isDirty() {
        return head != head_initial;
    }


    public static class HydratedStack implements HydratedCitizen {
        private final Node head;

        public HydratedStack(Node head) {
            this.head = head;
        }

        public Stack dehydrate(long ptr, MultiversionedStm.MultiversionedTransaction transaction) {
            Stack stack = new Stack();
            stack.head = head;
            stack.head_initial = head;
            stack.transaction = transaction;
            stack.ptr = ptr;
            return stack;
        }
    }
}
