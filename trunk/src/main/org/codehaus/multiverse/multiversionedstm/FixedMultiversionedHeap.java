package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.transaction.IllegalVersionException;
import org.codehaus.multiverse.transaction.NoSuchObjectException;
import org.codehaus.multiverse.util.Latch;
import org.codehaus.multiverse.util.CheapLatch;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * A POC MultiversionedHeap implementation that is based on an array instead of a map. Since array access is
 * a lot faster than map access, it could perform better. Needs to be checked, so don't use this implementation
 * yet (a lot of stuff also is not implemented).
 *
 * @param <E>
 */
public final class FixedMultiversionedHeap<E> implements MultiversionedHeap<E> {

    private final AtomicReferenceArray<Cell<E>> cells;
    private final AtomicInteger nextFreeIndex = new AtomicInteger(0);

    public FixedMultiversionedHeap(int size) {
        cells = new AtomicReferenceArray<Cell<E>>(new Cell[size]);
    }

    public long createHandle() {
        return nextFreeIndex.getAndIncrement();
    }

    public E read(long handle, long version) {
        int index = (int) handle;

        Cell<E> cell = cells.get(index);
        if (cell == null)
            throw new NoSuchObjectException(handle);

        return cell.getContent(version);
    }

    public E read(long handle) {
        int index = (int) handle;
        Cell<E> cell = cells.get(index);

        if (cell == null || cell.isDeleted())
            throw new NoSuchObjectException(handle);

        return cell.content;
    }

    public boolean isDeleted(long handle) {
        int index = (int) handle;
        Cell cell = cells.get(index);
        return cell == null || cell.isDeleted();
    }

    public long readVersion(long handle) {
        int index = (int) handle;
        Cell cell = cells.get(index);
        if (cell == null)
            throw new NoSuchObjectException(handle);

        return cell.version;
    }

    public void write(long handle, long version, E content) {
        int index = (int) handle;
        Cell<E> oldCell = cells.get(index);
        Cell<E> newCell = new Cell<E>(oldCell, version, content);
        cells.set(index, newCell);

        newCell.wakeupListeners();
    }

    public void delete(long handle, long version) {
        throw new RuntimeException();
    }

    public Latch listen(long[] handles, long version) {
        Latch latch = new CheapLatch();
        for (long handle : handles) {
            int index = (int)handle;

            Cell<E> cell = cells.get(index);
            if(cell.isDeleted()){
                latch.open();
                return latch;
            }

            cell.addListener(latch, version);


            //if the latch already is opened, we are done, so the loop can end now.
            if (latch.isOpen())
                return latch;
        }

        return latch;
    }

    private static class Cell<E> {
        final Cell<E> parent;
        final long version;
        final E content;

        Cell(Cell<E> parent, long version, E content) {
            this.parent = parent;
            this.version = version;
            this.content = content;
        }

        boolean isDeleted() {
            return content == null;
        }

        public E getContent(long version) {
            Cell<E> cell = this;
            while (cell != null) {
                if (cell.version <= version)
                    return cell.content;
                cell = cell.parent;
            }

            throw new IllegalVersionException(version);
        }

        public void addListener(Latch latch, long version) {
            //To change body of created methods use File | Settings | File Templates.
        }

        public void wakeupListeners() {



        }
    }
}
