package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.core.Transaction;

import java.util.Iterator;


/**
 * A DehydratedStmObject is the immutable result of the dehydration of an {@link StmObject}. Each
 * DehydratedStmObject should be completely immutable.
 *
 * By using polymorfism ( {@link #hydrate(Transaction)} different classes can be instantiated.
 *
 * @author Peter Veentjer.
 * @see StmObject
 */
public abstract class DehydratedStmObject {

    private final long handle;

    public DehydratedStmObject(){
        handle = 0;
    }

    public DehydratedStmObject(long handle) {
        this.handle = handle;
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
     * @return an iterator over all direct-handles.
     * @see StmObject#___loadedMembers()
     */
    public abstract Iterator<Long> members();

    /**
     * Hydrates a HeapCell to a StmObject. This method is the inverse of {@link StmObject#___dehydrate()}.
     *
     * @param transaction the transaction the created StmObject is part of.
     * @return the created StmObject.
     * @see StmObject#___dehydrate()
     */
    public abstract StmObject hydrate(Transaction transaction);
}
