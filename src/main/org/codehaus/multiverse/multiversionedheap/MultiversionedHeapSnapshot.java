package org.codehaus.multiverse.multiversionedheap;

import org.codehaus.multiverse.api.LockMode;
import org.codehaus.multiverse.util.iterators.PLongIterator;

/**
 * A snapshot of a {@link MultiversionedHeap}. It provides a read consistent view over the heap. So even
 * though the heap is modified by other transactions, these changes will not be visible in the snapshot.
 * So isolation problems like a dirty read, unrepeatable read, and phantom read are not allowed.
 *
 * @author Peter Veentjer.
 */
public interface MultiversionedHeapSnapshot<I extends Deflated> {

    /**
     * Reads the content at the specified handle. If no content is stored, null is returned. A null also
     * is returned when the handle is 0, since that reflects null.
     *
     * @param handle the handle of the HeapCell to read.
     * @return the read HeapCell, or null of the HeapCell at the specified handle doesn't exist or when handle is 0.
     * @see #readVersion(long)
     */
    I read(long handle);

    /**
     * Reads the version of the DehydratedStmObject at the specified handle. It can be that the version of the
     * DehydratedStmObject is older than the version of the transaction. The version will always be equal or smaller
     * than the version of this Snapshot.
     *
     * @param handle the handle of the DehydratedStmObject to load the version from.
     * @return the version of the DehydratedStmObject at the specified handle, or -1 if the DehydratedStmObject
     *         doesn't exist.
     * @see #read(long)
     * @see #getVersion()
     */
    long readVersion(long handle);

    /**
     * Returns the current lock for a specific handle.
     *
     * @param handle
     * @return the lock mode.
     *         todo: what to do if the handle doesn't exist.
     */
    LockMode readLockMode(long handle);

    /**
     * Returns the version of the snapshot.
     *
     * @return the version of the snapshot.
     */
    long getVersion();

    /**
     * Returns an iterator of the handles of root objects for this specific Snapshot. If an object is root
     * or not is important for garbage collection. If an object is root, or reachable from a root (directly
     * or indirectly) it can't be garbage collected.
     *
     * @return the handles to the root objects.
     */
    PLongIterator getRoots();
}
