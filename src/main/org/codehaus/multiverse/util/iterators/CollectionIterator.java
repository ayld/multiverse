package org.codehaus.multiverse.util.iterators;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class CollectionIterator<E> implements ResetableIterator<E> {
    private final Collection<E> collections;
    private Iterator<E> iterator;

    public CollectionIterator(Collection<E> collection) {
        if (collection == null) throw new NullPointerException();
        this.collections = collection;
    }

    public void reset() {
        iterator = null;
    }

    public boolean hasNext() {
        if (iterator == null)
            iterator = collections.iterator();

        return iterator.hasNext();
    }

    public E next() {
        if (!hasNext())
            throw new NoSuchElementException();

        return iterator.next();
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
