package org.codehaus.stm.transaction;

import java.util.Iterator;

public interface Transaction {

    /**
     * Attaches the object to the Transaction. Multiple calls to the attach method on the same transaction of the
     * same object are ignored. If other objects can be reached from the object, they are attached as well.
     *
     * What about objects that are not fresh.
     *
     * @param root the object to attach
     * @throws NullPointerException     if object is null.
     * @throws IllegalStateException    if this Transaction is not in the started state.
     * @throws IllegalArgumentException if the object already is attached to a different transaction or if
     *                                  object is not an {@link org.codehaus.stm.multiversionedstm.Citizen}.
     */
    void attach(Object root);

    /**
     * Reads the object
     *
     * @param ptr the pointer to the object.
     * @return the Object at the pointer.
     * @throws IllegalPointerException 
     */
    Object read(long ptr);

    /**
     * Returns an iterator over all addresses that were read by this transaction from main memory.
     *
     * @return
     */
    long[] getReadAddresses();

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
     * @throws AbortedTransaction    if the Transaction can't be committed.
     */
    void commit();

    /**
     * Rolls back the transaction. Multiple calls on the rollback method are ignored.
     *
     * @throws IllegalStateException if the Transaction already has been committed.
     */
    void abort();

    //todo: should this method be part of this sig.. this was added as a quick hack.
    long getVersion();

}
