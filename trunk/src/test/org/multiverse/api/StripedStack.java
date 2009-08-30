package org.multiverse.api;

import org.multiverse.api.annotations.AtomicObject;
import org.multiverse.utils.TodoException;

/**
 * @author Peter Veentjer
 */
@AtomicObject
public class StripedStack<E> {

    private final Stripe<E> stripe;

    public StripedStack(int stripeSize) {
        stripe = new DefaultStripe<E>(stripeSize);
    }

    public E pop() {
        throw new TodoException();
    }

    public void push(E item) {
        throw new TodoException();
    }

    private static class Node<E> {
        final E value;
        final Node<E> next;

        private Node(Node<E> next, E value) {
            this.next = next;
            this.value = value;
        }
    }
}
