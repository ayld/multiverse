package org.codehaus.multiverse.multiversionedheap.standard;

/**
 * A HeapNode is responsible for storing a {@link Block}.
 *
 * @param <B>
 */
public interface HeapNode<B extends Block> {

    long getHandle();

    B getBlock();

    /**
     * Writes the block. The HeapNode is not altered, but a new HeaoNode is returned instead.
     *
     * @param block
     * @param maximumVersion the version that is expected, this is needed for writeconflict detection.
     * @return the created HeapNode or null if there was a writeconflict.
     */
    HeapNode<B> write(B block, long maximumVersion);

    HeapNode<B> find(long handle);
}
