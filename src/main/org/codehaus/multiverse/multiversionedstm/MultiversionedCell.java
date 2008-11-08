package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.IllegalVersionException;
import org.codehaus.multiverse.util.Latch;

import java.util.concurrent.atomic.AtomicReference;

/**
 * The Heap is made up of cells.
 * <p/>
 * Each cell has a history of previous writes. This is required for the multiversioning functionality.
 */
public final class MultiversionedCell<E> {

    private volatile ContentNode<E> head;
    private final AtomicReference<LatchNode<E>> latchNodeHeadReference = new AtomicReference<LatchNode<E>>();

    public MultiversionedCell(E value, long version) {
        assert version >= 0;
        head = new ContentNode<E>(null, value, version);
    }

    /**
     * Returns the number of versions of values that are stored in this cell. After a cell is pruned,
     * the version count decreased.
     *
     * @return the number of versions of this cell.
     */
    public int getNumberOfVersions() {
        int result = 0;
        ContentNode headLocal = head;
        while (headLocal != null) {
            result++;
            headLocal = headLocal.parent;
        }
        return result;
    }

    public int getTotalSize() {
        int result = 0;
        ContentNode headLocal = head;
        while (headLocal != null) {
            //todo
            headLocal = headLocal.parent;
        }
        return 0;
    }

    /**
     * Prunes all content with a version older than the minimal version.
     *
     * @param minimalVersion the oldest version that is allowed to remain.
     */
    public void prune(long minimalVersion) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the current value of this cell.
     * <p/>
     * Can be called all the time by concurrent threads.
     *
     * @return the active value of this cell.
     */
    public Object getActiveValue() {
        //since head always is not equal to null, this gives no problems
        return head.value;
    }

    /**
     * Returns the version of the current value.
     * <p/>
     * Can be called all the time by concurrent threads.
     *
     * @return  the active version of the current value
     */
    public long getActiveVersion() {
        //since head always is not equal to null, this gives no problems
        return head.version;
    }

    /**
     * Returns the value of this cell for a specific version. The version of the value can be older than
     * the provided version.
     * <p/>
     * Can be called all the time by concurrent threads.
     *
     * @param version  the version of the value to look for.
     * @return the found value.
     * @throws IllegalVersionException if the transactionVersion is older than the oldest version.
     */
    public E getValue(long version) {
        assert version >= 0;

        ContentNode<E> headLocal = head;
        while (headLocal != null) {
            if (headLocal.version <= version)
                return headLocal.value;
            headLocal = headLocal.parent;
        }

        throw new IllegalVersionException(version);
    }

    /**
     * Adds a listener for change. If a change is made to a version of the data after
     * <p/>
     * Can be called by concurrent threads all the time.
     * <p/>
     * Can it be that the version doesn't exist yet?
     * - since the transaction version is based on an existing version of data, and the transaction
     * version is used to listen for change, the version (or a more older one) must exist.
     *
     * @param transactionVersion
     * @param latch the Latch to open
      */
    public void listen(long transactionVersion, Latch latch) {
        assert transactionVersion >= 0;
        assert latch != null;

        ContentNode<E> localContentHead = head;
        if (localContentHead.version > transactionVersion) {
            //a write has been made, so the latch can be openend we are done.
            latch.open();
        } else {
            //no write has been made, so we need to store the latch so it can be openend
            //when a write happens.
            LatchNode<E> newLatchNodeHead;
            LatchNode<E> oldLatchNodeHead;
            do {
                oldLatchNodeHead = latchNodeHeadReference.get();
                newLatchNodeHead = new LatchNode<E>(oldLatchNodeHead, transactionVersion, latch);
            } while (!latchNodeHeadReference.compareAndSet(oldLatchNodeHead, newLatchNodeHead));

            if (localContentHead != head) {
                //a write has been made, the latch can be openend.
                latch.open();
            } else {
                //no write has been made,it is now the responsibility of the writing thread to open
                //the latch.
            }
        }
    }

    /**
     * Writes a value to this cell.
     * <p/>
     * This method should not be called concurrently. But it can execute concurrently with other methods
     * of this MultiversionedCell.
     *
     * @param version the version of the value
     * @param value   the value itself.
     */
    public void write(long version, E value) {
        if (head.version >= version)
            throw new IllegalVersionException(version);

        head = new ContentNode<E>(head, value, version);

        //localizes all the latches.
        LatchNode latchNode = latchNodeHeadReference.getAndSet(null);
        //if a listen is done after getAndSet, it is the task of the listenenToChange thread
        //to open the latch.
        //if a listen is done before getAndSet, the write thread will open the latch.

        while (latchNode != null) {
            latchNode.latch.open();
            latchNode = latchNode.parent;
        }
    }

    //should be immutable
    static class ContentNode<E> {
        final ContentNode<E> parent;
        final long version;
        final E value;

        ContentNode(ContentNode<E> parent, E value, long version) {
            this.parent = parent;
            this.version = version;
            this.value = value;
        }
    }

    //should be immutable
    static class LatchNode<E> {
        final LatchNode<E> parent;
        final long version;
        final Latch latch;

        LatchNode(LatchNode<E> parent, long version, Latch latch) {
            this.parent = parent;
            this.version = version;
            this.latch = latch;
        }
    }
}
