package org.codehaus.multiverse.multiversionedheap.standard;

import org.codehaus.multiverse.multiversionedheap.Deflated;

/**
 * A BLock is the stuff that is stored in the heap. A Block contains the inflatable, but can also contain
 * implementation specific stuff like locking information. It would be possible to store the Inflatable directly
 * in the heap, and this was the original design. But because the Inflatable implementation is the responsibility
 * of heap, it is hard (almost impossible) that extra fields.
 *
 * @param <I>
 */
public interface Block<I extends Deflated> {

    long getHandle();

    I getInflatable();
}
