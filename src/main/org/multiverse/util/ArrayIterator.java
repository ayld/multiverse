package org.multiverse.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An {@link Iterator} implementation that traverses over an array. Removal through this Iterator
 * is not supported.
 * <p/>
 * This implementation is not threadsafe.
 *
 * @author Peter Veentjer.
 * @param <E>
 */
public final class ArrayIterator<E> implements Iterator {
    private final E[] array;
    private int index = -1;

    public ArrayIterator(E... array) {
        if (array == null) throw new NullPointerException();
        this.array = array;
    }

    /**
     * Resets this iterator.
     */
    public void reset() {
        index = -1;
    }

    @Override
    public boolean hasNext() {
        return index + 1 < array.length;
    }

    @Override
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
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
