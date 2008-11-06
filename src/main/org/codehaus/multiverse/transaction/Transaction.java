package org.codehaus.multiverse.transaction;

public interface Transaction {

    /**
     * Attaches the object to the Transaction. Multiple calls to the attach method on the same transaction of the
     * same object are ignored. Roots that are not fresh can be attached again (call will be ignored). If other
     * objects can be reached from the object, they are attached as well.
     *
     * @param root the object to attach
     * @throws NullPointerException     if object is null.
     * @throws IllegalStateException    if this Transaction is not in the started state.
     * @throws IllegalArgumentException if the object already is attached to a different transaction or if
     *                                  object is not an {@link org.codehaus.multiverse.multiversionedstm.Citizen}.
     */
    long attachRoot(Object root);

    /**
     * Reads a root object.
     *
     * @param ptr the pointer to the object.
     * @return the Object at the pointer.
     * @throws org.codehaus.multiverse.IllegalPointerException
     */
    Object readRoot(long ptr);

    /**
     * Removes a root object from this transaction. When this transaction commits, it doesn't mean that the
     * object is removed, it is only removed when the garbage collector detects that the object is not refered
     * by other objects.
     *
     * todo: what to do if object not part of transaction
     * todo: what to do if object already is removed
     *
     * @param ptr
     *
     */
    void deleteRoot(long ptr);

    /**
     * Returns the status of this Transaction
     *
     * @return the status of this Transaction
     */
    TransactionStatus getStatus();

    /**
     * Commits the changes to STM. Multiple commits are ignored.
     *
     * @throws IllegalStateException if the Transaction is aborted.
     * @throws AbortedException    if the Transaction can't be committed.
     */
    void commit();

    /**
     * Rolls back the transaction. Multiple calls on the rollback method are ignored.
     *
     * @throws IllegalStateException if the Transaction already has been committed.
     */
    void abort();
}
