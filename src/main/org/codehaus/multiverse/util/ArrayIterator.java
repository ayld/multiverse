package org.codehaus.multiverse.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ArrayIterator<E> implements Iterator<E> {
    private final E[] array;
    private int index = -1;

    public ArrayIterator(E... array) {
        if (array == null) throw new NullPointerException();
        this.array = array;
    }

    public boolean hasNext() {
        return index+1 < array.length;
    }

    public E next() {
        if (!hasNext())
            throw new NoSuchElementException();

        index++;
        return array[index];
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
