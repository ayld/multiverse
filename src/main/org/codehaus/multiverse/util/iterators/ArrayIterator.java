package org.codehaus.multiverse.util.iterators;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An {@link Iterator} that iterates over an array. It prevents converting the array to a {@link java.util.List}.
 * It doesn't support removal of items.
 * <p/>
 * This class is not threadsafe.
 *
 * @author Peter Veentjer.
 * @param <E>
 */
public final class ArrayIterator<E> implements ResetableIterator<E> {
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
