package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.transaction.ObjectDoesNotExistException;
import org.codehaus.multiverse.util.CheapLatch;
import org.codehaus.multiverse.util.Latch;
import static org.codehaus.multiverse.util.PtrUtils.versionIsValid;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A Heap tailored for Multiversioning. The MultiversionedHeap is contains MultiversionedCells and each of those
 * cells can contain multiple versions of content.
 *
 * @author Peter Veentjer.
 */
public final class GrowingMultiversionedHeap<E> implements MultiversionedHeap<E> {

    //statistics
    //the total number of reads.
    private final AtomicLong readCount = new AtomicLong();
    //the total number of writes.
    private final AtomicLong writeCount = new AtomicLong();

    private final ConcurrentMap<Long, MultiversionedCell<E>> cells = new ConcurrentHashMap<Long, MultiversionedCell<E>>();

    private final AtomicLong firstFreeHandle = new AtomicLong();

    /**
     * Returns the number of writes that have been executed.
     * <p/>
     * Method is threadsafe.
     *
     * @return the number of writes that have been executed.
     */
    public long getWriteCount() {
        return writeCount.longValue();
    }

    /**
     * Returns the number of reads that have happened.
     * <p/>
     * Method is threadsafe.
     *
     * @return the number of reads that have happened.
     */
    public long getReadCount() {
        return readCount.longValue();
    }

    /**
     * Returns the number of cells allocated in this heap.
     * <p/>
     * Method is threadsafe.
     *
     * @return the number of cells in this heap.
     */
    public int getCellCount() {
        return cells.size();
    }

    /**
     * Returns the number of versions in each cell and adds them. It gives a rough
     * indication of the size of the heap.
     * <p/>
     * Method is threadsafe.
     *
     * @return the found number of version.
     */
    public int getVersionCount() {
        int count = 0;
        for (MultiversionedCell cell : cells.values())
            count += cell.getStoredVersionCount();

        return count;
    }

    /**
     * Returns the number of versions a cell has.
     * <p/>
     * Method is threadsafe.
     *
     * @param handle the handle to the cell to check
     * @return the number of versions a cell has
     * @throws org.codehaus.multiverse.transaction.ObjectDoesNotExistException
     *          if cell doesn't exist.
     */
    public int getVersionCount(long handle) {
        MultiversionedCell cell = getExistingCell(handle);
        return cell.getStoredVersionCount();
    }

    public long createHandle() {
        return firstFreeHandle.incrementAndGet();
    }

    public Latch listen(long[] handles, long version) {
        assert versionIsValid(version);

        Latch latch = new CheapLatch();
        for (long handle : handles) {
            MultiversionedCell<E> cell = getExistingCell(handle);
            cell.listen(version, latch);

            //if the latch already is opened, we are done, so the loop can end now.
            if (latch.isOpen())
                return latch;
        }

        return latch;
    }

    public E read(long handle, long version) {
        assert versionIsValid(version);

        MultiversionedCell<E> cell = getExistingCell(handle);
        return cell.read(version);
    }

    private MultiversionedCell<E> getExistingCell(long handle) {
        MultiversionedCell<E> cell = cells.get(handle);
        if (cell == null)
            throw new ObjectDoesNotExistException(handle);

        readCount.incrementAndGet();
        return cell;
    }

    public E read(long handle) {
        MultiversionedCell<E> cell = cells.get(handle);
        if(cell == null)
            throw new ObjectDoesNotExistException();
        return cell.read();
    }

    //todo: what is the difference between deleted and non existing
    public boolean isDeleted(long handle) {
        MultiversionedCell<E> cell = cells.get(handle);
        return cell == null || cell.isDeleted();
    }

    public long readVersion(long handle) {
        MultiversionedCell cell = cells.get(handle);
        return cell == null ? -1 : cell.readVersion();
    }

    public void write(long handle, long version, E content) {
        assert versionIsValid(version);
        assert content != null;

        MultiversionedCell<E> cell = cells.get(handle);
        if (cell == null) {
            //the cell does not exist yet, so create it and store it.
            cell = new MultiversionedCell<E>(content, version);
            cells.put(handle, cell);
        } else {
            //the cell exist, so we can continue and do the write.
            cell.write(version, content);
        }

        writeCount.incrementAndGet();
    }

    public void delete(long handle, long version) {
        throw new RuntimeException();
    }

    /**
     * Prunes each cell in the heap. It removes all old versions that are not needed anymore.
     *
     * @param minimalVersion
     */
    public void prune(long minimalVersion) {
        assert versionIsValid(minimalVersion);

        for (MultiversionedCell cell : cells.values())
            cell.prune(minimalVersion);
    }
}
