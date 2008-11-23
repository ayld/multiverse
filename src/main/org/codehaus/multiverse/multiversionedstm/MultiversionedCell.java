package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.transaction.BadVersionException;
import org.codehaus.multiverse.transaction.NoSuchObjectException;
import org.codehaus.multiverse.util.Latch;
import static org.codehaus.multiverse.util.PtrUtils.versionIsValid;

import java.util.concurrent.atomic.AtomicReference;

/**
 * The Heap is made up of cells. Each cell has a history of previous writes. This is required for the multiversioning
 * functionality.
 *
 * @author Peter Veentjer.
 */
public final class MultiversionedCell<E> {

    private volatile ContentNode<E> head;
    private final AtomicReference<LatchNode<E>> latchNodeHeadReference = new AtomicReference<LatchNode<E>>();

    public MultiversionedCell(E content, long version) {
        assert version >= 0;
        head = new ContentNode<E>(null, content, version);
    }

    /**
     * Returns the number of versions of values that are stored in this cell. After a cell is pruned,
     * the version count decreases.
     * <p/>
     * Is threadsafe to call. Returned value could be stale as soon as it is received.
     *
     * @return the number of versions of this cell.
     */
    public int getStoredVersionCount() {
        int result = 0;
        ContentNode headLocal = head;
        while (headLocal != null) {
            result++;
            headLocal = headLocal.parent;
        }
        return result;
    }

    /**
     * Reads the current value of this cell.
     * <p/>
     * Is threadsafe to call. Returned value could be stale as soon as it is received.
     *
     * @return the current content of this cell.
     * @throws NoSuchObjectException if the cell already has been deleted
     */
    public E read() {
        //since head always is not equal to null, this gives no problems
        E result = head.content;
        if (result == null) //a null indicates a delete 
            throw new NoSuchObjectException();
        return result;
    }

    /**
     * Reads the current version of the cell.
     * <p/>
     * Is threadsafe to call. Returned value could be stale as soon as it is received.
     *
     * @return the active version of the current value
     */
    public long readVersion() {
        //since head always is not equal to null, this gives no problems
        return head.version;
    }

    /**
     * Returns the value of this cell for a specific version. The version of the value can be older than
     * the provided version.
     * <p/>
     * Can be called all the time by concurrent threads.
     * <p/>
     * todo: what to do if value has been deleted
     *
     * @param version the version of the value to look for.
     * @return the found value.
     * @throws org.codehaus.multiverse.transaction.BadVersionException if the transactionVersion is older than the oldest version.
     */
    public E read(long version) {
        assert versionIsValid(version);

        ContentNode<E> headLocal = head;
        while (headLocal != null) {
            if (headLocal.version <= version) {
                if (headLocal.isDeleted())
                    throw new NoSuchObjectException();

                return headLocal.content;
            }
            headLocal = headLocal.parent;
        }

        throw new BadVersionException(version);
    }

    public boolean isDeleted() {
        return head.isDeleted();
    }

    /**
     * Adds a listener for change. If a change is made to a version of the data after
     * <p/>
     * Can be called by concurrent threads all the time.
     * <p/>
     * Can it be that the version doesn't exist yet?
     * - since the transaction version is based on an existing version of data, and the transaction
     * version is used to listen for change, the version (or a more older one) must exist.
     * <p/>
     * todo: what to do if the value already has been deleted
     *
     * @param version
     * @param latch   the Latch to open
     */
    public void listen(long version, Latch latch) {
        assert versionIsValid(version);
        assert latch != null;

        ContentNode<E> localContentHead = head;
        if (localContentHead.version > version) {
            //a write has been made, so the latch can be openend and we are done.
            latch.open();
        } else {
            //no write has been made, so we need to store the latch so it can be openend
            //when a write happens.
            LatchNode<E> newLatchNodeHead;
            LatchNode<E> oldLatchNodeHead;
            do {
                oldLatchNodeHead = latchNodeHeadReference.get();
                newLatchNodeHead = new LatchNode<E>(oldLatchNodeHead, latch, version);
            } while (!latchNodeHeadReference.compareAndSet(oldLatchNodeHead, newLatchNodeHead));

            if (localContentHead != head) {
                //a write has been made in the meanwhile, the latch can be openend.
                latch.open();
            } else {
                //no write has been made,it is now the responsibility of the writing thread to open the latch.
            }
        }
    }

    /**
     * Mark this cell as deleted. The cell is not be deleted immediately, because it could be needed by
     * transactions that are still reading previous versions of content. For them this cell still exists
     * and isn't deleted.
     * <p/>
     * todo: what to do if the value already has been deleted.
     *
     * @param version the version of the stm where this MultiversionedCell has been deleted.
     */
    public void delete(long version) {
        writeOrDelete(version, null);
    }

    /**
     * Writes a value to this cell.
     * <p/>
     * This method should not be called concurrently with other changing method. But it can execute concurrently with
     * other read-only methods of this MultiversionedCell.
     *
     * @param version the version of the value
     * @param value   the value itself.
     * @throws org.codehaus.multiverse.transaction.BadVersionException if the version is older than the next recent value
     * @throws NoSuchObjectException   if the cell already has been deleted
     */
    public void write(long version, E value) {
        assert value != null;
        writeOrDelete(version, value);
    }

    /**
     * Writes a new value to the cell or deletes it. A non null value indicates a write, a null value indicates
     * a delete. Since the logic for both is the same, this method was created. It is private since the null/not null
     * meaning is an implementation detail.
     *
     * @param version
     * @param value   the value to write, can be null to indicate a delete.
     */
    private void writeOrDelete(long version, E value) {
        assert versionIsValid(version);

        if (head.version >= version)
            throw new BadVersionException(version);

        if (head.isDeleted())
            throw new NoSuchObjectException();

        head = new ContentNode<E>(head, value, version);

        //localizes all the latches.
        LatchNode latchNode = latchNodeHeadReference.getAndSet(null);
        //if a listen is done after getAndSet, it is the task of the listenen thread to open the latch.
        //if a listen is done before getAndSet, the write thread will open the latch.

        if (latchNode != null)
            latchNode.openAll();
    }

    /**
     * Prunes all content with a version older than the minimal version.
     *
     * @param minimalVersion the oldest version that is allowed to remain.
     */
    public void prune(long minimalVersion) {
        throw new UnsupportedOperationException();
    }


    //should remain immutable
    //a null content indicates that the cell has been deleted.
    static class ContentNode<E> {
        final ContentNode<E> parent;
        final long version;
        final E content;

        ContentNode(ContentNode<E> parent, E content, long version) {
            this.parent = parent;
            this.version = version;
            this.content = content;
        }

        boolean isDeleted() {
            return content == null;
        }
    }

    //should remain immutable
    static class LatchNode<E> {
        final LatchNode<E> parent;
        final long version;
        final Latch latch;

        LatchNode(LatchNode<E> parent, Latch latch, long version) {
            this.parent = parent;
            this.version = version;
            this.latch = latch;
        }

        void openAll() {
            LatchNode node = this;
            do {
                node.latch.open();
                node = node.parent;
            } while (node != null);
        }
    }
}
