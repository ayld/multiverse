package org.multiverse.stms.alpha.retry;

import static org.multiverse.api.StmUtils.retry;
import org.multiverse.api.annotations.AtomicObject;

@AtomicObject
public class RetryStack<E> {

    private int size;
    private Node<E> head;

    public int size() {
        return size;
    }

    public void push(E item) {
        if (item == null) {
            throw new NullPointerException();
        }

        head = new Node<E>(item, head);
        size++;
    }

    public E pop() {
        //problems:
        //-the retry mechanism doesn't know which objects should be used in the spin set.
        //    this problem could be solved by adding these objects explicitly
        //    this problem could be solved from the instrumentation
        //-the retry mechanism needs to undo the changes made
        //-the retry mechanism needs to re-execute the check

        if (size == 0) {
            retry();
        }

        Node<E> oldHead = head;
        head = head.next;
        return oldHead.value;
    }

    static class Node<E> {
        final E value;
        final Node<E> next;

        Node(E value, Node<E> next) {
            this.value = value;
            this.next = next;
        }
    }
}
