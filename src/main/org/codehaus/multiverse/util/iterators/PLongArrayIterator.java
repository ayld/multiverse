package org.codehaus.multiverse.util.iterators;

import java.util.NoSuchElementException;

public class PLongArrayIterator implements PLongIterator {
    private final long[] array;
    private int index = -1;

    public PLongArrayIterator(long... array) {
        if (array == null) throw new NullPointerException();
        this.array = array;
    }

    public void reset() {
        index = -1;
    }

    public boolean hasNext() {
        return index + 1 < array.length;
    }

    public long next() {
        if (!hasNext())
            throw new NoSuchElementException();

        index++;
        return array[index];
    }
}
