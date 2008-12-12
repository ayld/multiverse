package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.core.Transaction;

import java.util.Iterator;

/**
 * Each persistant object that wants to live in the {@link MultiversionedStm} needs to implement this interface.
 * In the end some kind of bytecode instrumentation can add this interface and implement the methods automatically
 * on certain pojo's.
 *
 * The reason that all methods have such a strange prefix, is that normal pojo's will be instrumented so that they
 * implement this interface. The prefix prevents name clashes.
 *
 * @author Peter Veentjer.
 */
public interface StmObject {

    /**
     * Attaches the StmObject to a transaction. As soon as an object is attached to a transaction, a handle
     * will be assigned to it.
     *
     * todo: what to do if the object already is attached to another transaction.
     * todo: what to do if the handle is 0 (so resembles a null).
     *
     * @param transaction the transaction this StmObject attaches to. The value should not be null.
     * @param handle the handle of the StmObject.
     */
    void ___onAttach(Transaction transaction, long handle);

    /**
     * Returns the Transaction this StmObject is part of. The transaction is set on an object when it is attached
     * to a transaction or loaded through a transaction. If the StmObject is not part of a Transaction, the returned
     * value is null.
     *
     * @return the Transaction this StmObject is part of.
     */
    Transaction ___getTransaction();

    /**
     * Returns an Iterator over all members. If a member is not loaded (lazy) it is not iterated over.
     * This is because we don't want to load complete objects graphs in heap.
     *
     * @return  the iterator over all direct referenced citizens.
     */
    Iterator<StmObject> ___loadedMembers();

    /**
     * Returns the handle of this StmObject in the heap. If the object is still transient, 0 is returned.
     *
     * @return the handle of this StmObject in the heap.
     */
    long ___getHandle();

    /**
     * Returns the initial DehydratedStmObject that was used to reconstruct this instance. This property only is
     * set when an StmObject is loaded by a transaction. If an object is new, there is no initial
     * DehydratedStmObject.
     *
     * @return the initial DehydratedStmObject, or null if not present.
     */
    DehydratedStmObject ___getInitialDehydratedStmObject();

    /**
     * Dehydrates this {@link org.codehaus.multiverse.multiversionedstm.StmObject} to an object that can be stored
     * in the heap. The result of this dehydration is a snapshot of how this StmObject looked like when the
     * hydration occurred.
     *
     * @return the result of the dehydration.
     */
    DehydratedStmObject ___dehydrate();

    /**
     * Checks if this StmObject needs to be written to heap when the transaction commits. Changes in
     * StmObjects that can be reached from this StmObject don't matter.
     *
     * @return true if this StmObject is dirty (so has changes).
     */
    boolean ___isDirty();
}
