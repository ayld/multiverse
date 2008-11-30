package org.codehaus.multiverse.multiversionedstm;

/**
 * A Snapshot of a {@link Heap}. It provides a read consistent view over the heap. So even though the heap
 * is modified by other transaction, it will not be visible in this HeapSnapshot.
 *
 * @author Peter Veentjer.
 */
public interface HeapSnapshot {

    /**
     * Returns the version of the snapshot.
     *
     * @return the version of the snapshot.
     */
    long getVersion();

    /**
     * Returns the version of the HeapCell at the specified handle. It can be that the version of the
     * HeapCell is older than the version of the transaction. This can happen when the HeapCell is older. The
     * version will always be equal or smaller than the version of this Snapshot.
     *
     * @param handle the handle of the DehydratedStmObject to load the version from.
     * @return the version of the DehydratedStmObject at the specified handle, or -1 if the DehydratedStmObject
     *         doesn't exist.
     */
    long getVersion(long handle);

    /**
     * Returns an array of handles of root objects for this specific Snapshot.
     *
     * array should not be modified!
     *
     * @return
     */
    long[] getRoots();

    /**
     * Reads the HeapCell at the specified handle. If the HeapCell doesn't exist, null is returned.
     *
     * @param handle the handle of the HeapCell to read.
     * @return the read HeapCell, or null of the HeapCell at the specified handle doesn't exist.
     */
    DehydratedStmObject read(long handle);
}
