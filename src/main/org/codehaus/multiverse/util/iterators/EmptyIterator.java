package org.codehaus.multiverse.util.iterators;

import java.util.Iterator;

/**
 * An {@link Iterator} that is a special case iterator when you don't need to iterate over elements.
 *
 * It is threadsafe to use, because there isn't any shared state.
 *
 * @param <E>
 * @author Peter Veentjer
 */
public final class EmptyIterator<E> implements Iterator<E> {

    public final static EmptyIterator INSTANCE = new EmptyIterator();

    public boolean hasNext() {
        return false;
    }

    public E next() {
        throw new UnsupportedOperationException();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
