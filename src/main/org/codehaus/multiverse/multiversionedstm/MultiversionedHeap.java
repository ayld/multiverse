package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.util.Latch;

/**
 *
 *
 * @param <E>
 * @author Peter Veentjer.
 */
public interface MultiversionedHeap<E> {

    /**
     * Creates a new handle that can be used to find an object (it could be compared to an alloc function).
     * <p/>
     * Method is threadsafe.
     * <p/>
     * It could be that handles are created, but eventually are not written to main memory. An example is a
     * transaction that is aborted.
     *
     * @return the created handle.
     */
    long createHandle();

    /**
     * Reads the content of a specific memory cell.
     *
     * @param handle  the handle of the cell.
     * @param version the version of the cell. If a cell with the wanted version is not found, a cell with
     *                an older version are used (the least old version).
     * @return the content of the memory cell.
     * @throws org.codehaus.multiverse.transaction.NoSuchObjectException
     *          if pointer points to a non existing address.
     * @throws org.codehaus.multiverse.transaction.IllegalVersionException
     *          if the version is not valid, or if no cell with the desired version (or older)
     *          is found.
     */
    E read(long handle, long version);

    /**
     * Reads the current value of a specific cell in the heap.
     * <p/>
     * Method is threadsafe.
     *
     * @param handle the handle of the cell
     * @return the actual value of the cell
     * @throws org.codehaus.multiverse.transaction.NoSuchObjectException
     *          if the handle points to a non existing cell.
     */
    E read(long handle);

    boolean isDeleted(long handle);

    /**
     * Returns the version of the most recent write for a specific cell.
     *
     * @param handle the handle of the cell.
     * @return the version of the last write to the specific cell.
     * @throws org.codehaus.multiverse.transaction.NoSuchObjectException if the handle points to a non existing cell.
     */
    long readVersion(long handle);

    /**
     * Writes a value to the heap.
     *
     * This method should only be called by a single thread. It can be called parallel to
     * the other readonly methods of this heap.
     *
     * The #createHandle method should be used to allocate new memory.
     *
     * @param handle  the handle of the cell to write to.
     * @param version the version of the cell
     * @param content the content to write.
     * @throws NullPointerException    if content is null
     * @throws org.codehaus.multiverse.transaction.NoSuchObjectException  if the handle points to a non existing cell.
     * @throws org.codehaus.multiverse.transaction.IllegalVersionException if the version of the cell doesn't exist.
     */
    void write(long handle, long version, E content);

    /**
     * Deletes a value from the heap. A delete doesn't have to mean that the cell is completely deleted because
     * current transactions still could rely on a previous value. The delete only is visibile to newer transactions.
     *
     * @param handle   the handle of the dell to the delete.
     * @param version  the version
     * @throws org.codehaus.multiverse.transaction.NoSuchObjectException if the handle points to a non existing object
     *              (this also goes for deleted objects).
     * @throws org.codehaus.multiverse.transaction.IllegalVersionException if the version of the cell doesn't exist or
     */
    void delete(long handle, long version);

    /**
     * Creates a {@link Latch} that is opened when a write is done on of the handle.
     * <p/>
     * Method is threadsafe.
     *
     * @param version
     * @param handles
     * @return the created Latch.
     */
    Latch listen(long[] handles, long version);
}
