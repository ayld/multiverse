package org.codehaus.multiverse.transaction;


/**
 *
 * A Transaction can be compared to a Hibernate Session. 
 *
 * A Transaction is not threadsafe and should not be shared between threads, unless it is safely moved from
 * one thread to another. todo: think about the future need for a threadlocal to store the transaction
 *
 * @author Peter Veentjer
 */
public interface Transaction {

    /**
     * Attaches the object to the Transaction. Multiple calls to the attach method on the same transaction of the
     * same object are ignored. Roots that are not fresh can be attached again (call will be ignored). If other
     * objects can be reached from the object, they are attached as well.
     *
     * This method is not threadsafe.
     *
     * @param root the object to attach
     * @throws NullPointerException     if object is null.
     * @throws IllegalStateException    if this Transaction is not in the started state.
     * @throws IllegalArgumentException if object is not an object that can be stored in this transaction. A specific
     *                                  type for the root, can't be used, because compile time the instrumentation
     *                                  of additional interfaces has not been done.
     * @throws BadTransactionException if root, or any other citizen that can be reached through this,
     *                                  root,  already is attached to a different transaction
     * @return the address of the object. If the transaction is not committed, this address is not valid.
     */
    long attach(Object root);

    /**
     * Reads an object.
     *
     * This method is not threadsafe.
     *
     * @param handle the handle to the object.
     * @return the Object at the pointer.
     * @throws NoSuchObjectException
     */
    Object read(long handle);

    /**
     * Removes a root object from this transaction. When this transaction commits, it doesn't mean that the
     * object is removed, it is only removed when the garbage collector detects that the object is not refered
     * by other objects. If the object already is deleted within this transaction, the call is ignored.
     * todo: what if the object doesn't exist?
     *
     * This method is not threadsafe.
     *
     * todo: what to do if object not part of transaction
     * todo: what to do if object already is removed
     * todo: what to do if the object does not exist
     *
     * @param handle the pointer of the object to remove.
     */
    void delete(long handle);

    /**
     * Deletes the root from this transaction. As soon as the transaction commits, it will not be visibile to
     * new transactions anymore. If the object already is deleted from this Transaction, the call is ignored.
     *
     * @param root
     * @throws NullPointerException if root is null
     * @throws BadTransactionException if root is attached to another transaction, or if root object is attached to
     *         no transaction.
     * @throws IllegalArgumentException if the root is not an object that can be deleted from this transaction because
     *         it has a bad type.
     * @throws IllegalStateException if the Transaction already is committed or aborted.
     */
    void delete(Object root);

    /**
     * Returns the status of this Transaction
     *
     * This method is threadsafe.
     *
     * @return the status of this Transaction
     */
    TransactionStatus getStatus();

    /**
     * Commits the changes to STM. Multiple commits are ignored.
     *
     * This method is not threadsafe.
     *
     * todo: what to do if the transaction can't commit because it is realyonly (not important for now)
     * todo: what to do if the transaction can't commit because it's content is attached to a different transaction
     *
     * @throws IllegalStateException if the Transaction is already aborted.
     * @throws AbortedException    if the Transaction can't be committed.
     */
    void commit();

    /**
     * Rolls back the transaction. Multiple calls on the abort method are ignored.
     *
     * This method is not threadsafe.
     *
     * @throws IllegalStateException if the Transaction already has been committed.
     */
    void abort();
}
