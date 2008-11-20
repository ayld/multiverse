package org.codehaus.multiverse.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A {@link Iterator} that iterates over an array. It prevents converting the array to a {@link java.util.List}.
 * It doesn't support removal of items.
 *
 * This class is not threadsafe.
 *
 * @param <E>
 * @author Peter Veentjer.
 */
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

    /**
     * This method is not supported.
     *
     * @throws UnsupportedOperationException
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
