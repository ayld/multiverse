package org.codehaus.multiverse.util.iterators;

import java.util.Iterator;

public interface ResetableIterator<E> extends Iterator<E> {

    void reset();
}
