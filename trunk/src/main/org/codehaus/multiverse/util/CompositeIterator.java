package org.codehaus.multiverse.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class CompositeIterator<E> implements Iterator<E> {
    private final Iterator<E>[] iterators;
    private Iterator<E> currentIterator;
    private int index;

    public CompositeIterator(Iterator<E>... iterators) {
        this.iterators = iterators;
        if (iterators.length > 0) {
            index = 0;
            currentIterator = iterators[index];
        } else {
            currentIterator = EmptyIterator.INSTANCE;
            index = -1;
        }
    }

    public boolean hasNext() {
        do {
            if (currentIterator.hasNext())
                return true;
        } while (findNextIterator());

        return false;
    }

    private boolean findNextIterator() {
        if (index + 1 >= iterators.length)
            return false;

        index++;
        currentIterator = iterators[index];
        return true;
    }

    public E next() {
        if (hasNext())
            return currentIterator.next();

        throw new NoSuchElementException();
    }

    public void remove() {
        currentIterator.remove();
    }
}
