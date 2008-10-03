package org.codehaus.stm.util;

import java.util.Iterator;

public class EmptyIterator<E> implements Iterator<E> {

    public final static EmptyIterator INSTANCE = new EmptyIterator();

    public boolean hasNext() {
        return false;
    }

    public E next() {
        throw new RuntimeException();
    }

    public void remove() {
        throw new RuntimeException();
    }
}
