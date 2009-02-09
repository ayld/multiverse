package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.core.Transaction;

import java.util.Iterator;


/**
 * A DehydratedStmObject is the immutable result of the dehydration of an {@link StmObject}. Each
 * DehydratedStmObject should be completely immutable. An DehydratedStmObject is not attached to a
 * transaction, since the same DehydratedStmObject can be used by multiple transactions simultaniously.
 * <p/>
 * <p/>
 * By using polymorfism, ( {@link #hydrate(Transaction)} different classes can be instantiated.
 *
 * @author Peter Veentjer.
 * @see StmObject
 */
public abstract class DehydratedStmObject {

    private final long handle;
    private long version;

    public DehydratedStmObject(long handle) {
        assert handle != 0;
        this.handle = handle;
    }

    /**
     * Sets the version of this DehydratedStmObject. The version should only be set once. It can be set
     * at construction time because the version is not known at that time. And there needs to be a happens
     * before relation between the write and the reaf this field, this happens before relation is introduced
     * by the atomicreference that contains the active snapshot in the  MultiversionedHeapSnapshotChain.
     *
     * @param version the version to set.
     */
    public void setVersion(long version) {
        this.version = version;
    }

    /**
     * Gets the version of this DehydratedStmObject.
     *
     * @return the version of this DehydratedStmObject.
     */
    public long getVersion() {
        return version;
    }

    /**
     * Returns the handle of the DehydratedStmObject
     *
     * @return the handle of the DehydratedStmObject.
     * @see StmObject#___getHandle()
     */
    public long getHandle() {
        return handle;
    }

    /**
     * Returns an iterator containing all handles to the member {@link DehydratedStmObject}. It could be that the
     * iterator contains the handle of the object itself. So if cycles become an issue, the caller should take
     * care of this.
     *
     * @return an iterator over all handles to member DehydratedStmObjects.
     * @see StmObject#___getFreshOrLoadedStmMembers()
     */
    public abstract Iterator<Long> members();

    /**
     * Hydrates a DehydratedStmObject to a StmObject. This method is the inverse of {@link StmObject#___dehydrate()}.
     * If the DehydratedStmObject is truly immutable, the same instance needs to be returned. The transaction is
     * not responsible for this, so there is no load on the transaction if immutable objects are used.
     *
     * @param transaction the transaction the created StmObject is part of.
     * @return the created StmObject.
     * @see StmObject#___dehydrate()
     * @see StmObject#___isImmutable()
     */
    public abstract StmObject hydrate(Transaction transaction);
}
