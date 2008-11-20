package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.transaction.IllegalVersionException;
import org.codehaus.multiverse.transaction.ObjectDoesNotExistException;
import org.codehaus.multiverse.util.Latch;
import org.codehaus.multiverse.util.CheapLatch;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class FixedMultiversionedHeap<E> implements MultiversionedHeap<E> {

    private final AtomicReferenceArray<Cell> cells;
    private final AtomicInteger nextFreeIndex = new AtomicInteger();

    public FixedMultiversionedHeap(int size) {
        cells = new AtomicReferenceArray<Cell>(new Cell[size]);
    }

    public long createHandle() {
        return nextFreeIndex.getAndIncrement();
    }

    public E read(long handle, long version) {
        int index = (int) handle;

        Cell<E> cell = cells.get(index);
        if (cell == null)
            throw new ObjectDoesNotExistException(handle);

        return cell.getContent(version);
    }

    public E read(long handle) {
        int index = (int) handle;
        Cell<E> cell = cells.get(index);
        if (cell == null)
            throw new ObjectDoesNotExistException(handle);

        if (cell.content == null)
            throw new ObjectDoesNotExistException(handle);

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
            throw new ObjectDoesNotExistException(handle);

        return cell.version;
    }

    public void write(long handle, long version, E content) {
        int index = (int) handle;
        Cell cell = cells.get(index);
        Cell newCell = new Cell(cell, version, content);
        cells.set(index, newCell);
    }

    public void delete(long handle, long version) {
        throw new RuntimeException();
    }

    public Latch listen(long[] handles, long version) {
        Latch latch = new CheapLatch();
        for (long handle : handles) {
            int index = (int)handle;

            Cell<E> cell = cells.get(index);
            //cell.listen(version, latch);

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
    }
}
