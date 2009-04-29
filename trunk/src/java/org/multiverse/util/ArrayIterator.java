package org.multiverse.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public final class ArrayIterator<E> implements Iterator {
    private final E[] array;
    private int index = -1;

    public ArrayIterator(E... array) {
        if (array == null) throw new NullPointerException();
        this.array = array;
    }

    public void reset() {
        index = -1;
    }

    public boolean hasNext() {
        return index + 1 < array.length;
    }

    public E next() {
        if (!hasNext())
            throw new NoSuchElementException();

        index++;
        return array[index];
    }

    /**
     * This method is not supported.
     *
     * @throws UnsupportedOperationException
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
