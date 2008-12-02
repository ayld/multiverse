package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.transaction.Transaction;

import java.util.Iterator;


/**
 * A DehydratedStmObject is the immutable result of the dehydration of an {@link StmObject}.
 *
 * @author Peter Veentjer.
 */
public abstract class DehydratedStmObject {

    private final long handle;

    public DehydratedStmObject(){
        handle = 0;
    }

    protected DehydratedStmObject(long handle) {
        this.handle = handle;
    }

    public long getHandle() {
        return handle;
    }

    /**
     * Returns an iterator containing all direct-handles to other HeapCells (so all objects that are directly
     * reachble from this HeapCell).
     *
     * @return
     */
    public abstract Iterator<Long> getDirect();

    /**
     * Hydrates a HeapCell to the original Citizen. 
     *
     * @param transaction
     * @return
     */
    public abstract StmObject hydrate(Transaction transaction);
}
