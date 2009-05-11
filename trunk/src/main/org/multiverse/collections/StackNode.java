package org.multiverse.collections;

import org.multiverse.api.TmEntity;

@TmEntity
public class StackNode<E> {
    protected final StackNode<E> next;
    protected final E value;

    public StackNode(StackNode<E> next, E value) {
        this.next = next;
        this.value = value;
    }
}