package org.multiverse.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An {@link Iterator} that iterates over a single instance. Removal is not supported.
 * <p/>
 * Instance of not threadsafe to use.
 *
 * @author Peter Veentjer
 * @param <E>
 */
public final class InstanceIterator<E> implements Iterator<E> {
    private boolean nextCalled = false;
    private final E item;

    public InstanceIterator(E item) {
        if (item == null) throw new NullPointerException();
        this.item = item;
    }

    public void reset() {
        nextCalled = false;
    }

    @Override
    public boolean hasNext() {
        return !nextCalled;
    }

    @Override
    public E next() {
        if (!hasNext())
            throw new NoSuchElementException();

        nextCalled = true;
        return item;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
