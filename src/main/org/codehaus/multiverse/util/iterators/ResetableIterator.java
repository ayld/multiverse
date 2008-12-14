package org.codehaus.multiverse.util.iterators;

import java.util.Iterator;

/**
 * An {@link Iterator} that can be reset, so that it iterates over all elements again.
 *
 * @author Peter Veentjer.
 * @param <E>
 */
public interface ResetableIterator<E> extends Iterator<E> {

    /**
     * Resets this iterator, so that it can iterate over all elements again.
     */
    void reset();
}
