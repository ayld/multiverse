package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.core.Transaction;

import java.util.Iterator;

/**
 * Each persistant object that wants to live in the {@link MultiversionedStm} needs to implement this interface.
 * In the end some kind of bytecode instrumentation can add this interface and implement the methods automatically
 * on certain pojo's.
 * <p/>
 * The reason that all methods have such a strange prefix, is that normal pojo's will be instrumented so that they
 * implement this interface. The prefix prevents name clashes.
 * <p/>
 * todo:
 * what happens to an StmObject once the transaction has finished (commit or abort). Is it still attached
 * to a dead transaction.
 *
 * @author Peter Veentjer.
 */
public interface StmObject {

    /**
     * Attaches the StmObject to a transaction. No checks are done if the object already is connected to another
     * transaction. It is up the the stm implementation to deal with this.
     *
     * @param transaction the transaction this StmObject attaches to. The value should not be null.
     */
    void ___onAttach(Transaction transaction);

    /**
     * Returns the Transaction this StmObject is part of. The transaction is set on an StmObject when it is attached
     * to a transaction or loaded through a transaction. If the StmObject is new or detached, null is returned.
     * <p/>
     * todo:
     * atm not null also is returned when the transaction an stmobject belongs to has committed, or
     * aborted.
     *
     * @return the Transaction this StmObject is part of, or null if it isn't attached to a transaction.
     */
    Transaction ___getTransaction();

    /**
     * Returns an Iterator over all StmObject members. If a member is not loaded (lazy) it is not iterated over.
     * This is because we don't want to load complete objects graphs in heap. Other types of members, like primitives
     * and other non StmObjects, or ignored.
     *
     * @return the iterator over all loaded member StmObjects.
     */
    Iterator<StmObject> ___loadedMembers();

    /**
     * Returns the handle of this StmObject in the heap. A non 0 value always will be returned, no matter
     * if the object has never been persisted before.
     *
     * @return the handle of this StmObject in the heap.
     */
    long ___getHandle();

    /**
     * Returns the initial DehydratedStmObject that was used to reconstruct this instance. This property only is
     * set when an StmObject is loaded by a transaction. If an object is new, there is no initial
     * DehydratedStmObject.
     * <p/>
     * todo: what if the object is detached.
     *
     * @return the initial DehydratedStmObject, or null if not present.
     */
    DehydratedStmObject ___getInitialDehydratedStmObject();

    /**
     * Dehydrates this {@link StmObject} to an object that can be stored in the heap. The result of this
     * dehydration is a snapshot of the state of this StmObject. Any changes made to this StmObject after
     * the dehydration, should not be visible in the DehydratedStmObject (since each DehydratedStmObject is
     * considered to be immutable).
     * <p/>
     * todo: what if the object is not attached
     *
     * @return the result of the dehydration.
     */
    DehydratedStmObject ___dehydrate();

    /**
     * Checks if this StmObject needs to be written to heap when the transaction commits. Changes in
     * StmObjects that can be reached from this StmObject don't matter. A fresh StmObject is dirty by default.
     * <p/>
     * todo: what if the object is not attached
     *
     * @return true if this StmObject is dirty (so has changes), false otherwise.
     */
    boolean ___isDirty();
}
