package org.codehaus.multiverse.utils.iterators;

import java.util.NoSuchElementException;

/**
 * An {@link ResetableIterator} that iterates over a single instance.
 *
 * @author Peter Veentjer
 * @param <E>
 */
public class InstanceIterator<E> implements ResetableIterator<E> {
    private boolean nextCalled = false;
    private final E item;

    public InstanceIterator(E item) {
        if (item == null) throw new NullPointerException();
        this.item = item;
    }

    public void reset() {
        nextCalled = false;
    }

    public boolean hasNext() {
        return !nextCalled;
    }

    public E next() {
        if (!hasNext())
            throw new NoSuchElementException();

        nextCalled = true;
        return item;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
