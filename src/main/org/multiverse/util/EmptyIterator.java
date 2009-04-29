package org.multiverse.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An {@link java.util.Iterator} that is a special case iterator when you don't need to iterate over elements.
 * <p/>
 * It is threadsafe to use, because there isn't any shared state.
 *
 * @author Peter Veentjer
 * @param <E>
 */
public final class EmptyIterator<E> implements Iterator<E> {

    public final static EmptyIterator INSTANCE = new EmptyIterator();

    /**
     * Always returns false since there are no elements to iterate over.
     *
     * @return false.
     */
    @Override
    public boolean hasNext() {
        return false;
    }

    /**
     * Alaways throws an NoSuchElementException since there are no items to iterate over.
     *
     * @return can't return anything.
     */
    @Override
    public E next() {
        throw new NoSuchElementException();
    }

    /**
     * Always throwns an UnsupportedOperationException exceptions since this EmptyIterator doesn't contain
     * elements to remove.
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
