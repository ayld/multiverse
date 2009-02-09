package org.codehaus.multiverse.util.iterators;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An {@link Iterator} that is a special case iterator when you don't need to iterate over elements.
 * <p/>
 * It is threadsafe to use, because there isn't any shared state.
 *
 * @author Peter Veentjer
 * @param <E>
 */
public final class EmptyIterator<E> implements ResetableIterator<E> {

    public final static EmptyIterator INSTANCE = new EmptyIterator();

    /**
     * Always returns false since there are no elements to iterate over.
     *
     * @return false.
     */
    public boolean hasNext() {
        return false;
    }

    public void reset() {
        //ignore.
    }

    /**
     * Alaways throws an NoSuchElementException since there are no items to iterate over.
     *
     * @return can't return anything.
     */
    public E next() {
        throw new NoSuchElementException();
    }

    /**
     * Always throwns an UnsupportedOperationException exceptions since this EmptyIterator doesn't contain
     * elements to remove.
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
