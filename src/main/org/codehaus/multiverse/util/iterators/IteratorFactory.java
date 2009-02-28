package org.codehaus.multiverse.util.iterators;

import java.util.Iterator;

/**
 * todo:
 * ResetableIterator can be removed, since a new iterator can be returned instead of reset.
 *
 * @param <E>
 */
public interface IteratorFactory<E> {
    Iterator<E> create();
}