package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.transaction.Transaction;

import java.util.Iterator;

/**
 * Each persistant object that wants to live in the {@link MultiversionedStm} needs to implement this interface.
 * In the end some kind of bytecode instrumentation can add this interface and implement the methods automatically
 * on certain pojo's.
 *
 * @author Peter Veentjer.
 */
public interface StmObject {

    void ___onAttach(Transaction multiversionedTransaction);

    Transaction ___getTransaction();

    /**
     * Returns an Iterator over all direct referenced citizen.
     *
     * @return
     */
    Iterator<StmObject> ___directReferencedIterator();

    /**
     * Returns the handle of this StmObject in the heap. If the object is still transient, 0 is returned.
     *
     * @return
     */
    long ___getHandle();

    DehydratedStmObject ___getInitialDehydratedStmObject();

    /**
     * Sets the handle.
     * 
     * @param ptr
     */
    void ___setHandle(long ptr);

    DehydratedStmObject ___dehydrate();

    /**
     * Checks if this StmObject needs to be written to heap when the transaction commits. Changes in
     * StmObjects that can be reached from this StmObject don't matter.
     *
     * @return true if this StmObject is dirty (so has changes).
     */
    boolean ___isDirty();    
}
