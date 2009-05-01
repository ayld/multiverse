package org.multiverse.api;

/**
 * Unit of work.
 * <p/>
 * All changes made on objects reachable from this Transaction (attached, read, or reachable from those
 * 2) will be committed when the Transaction commits. Watch out for {@link #readUnmanaged(Originator)} btw.
 * <p/>
 * A Transaction is not threadsafe to use (although it can be handed over from thread to thread).
 *
 * @author Peter Veentjer.
 */
public interface Transaction {

    /**
     * Commits all changes made under this transaction to the STM. If the transaction
     * already is committed, this call is ignored.
     *
     * @throws org.multiverse.api.exceptions.WriteConflictException
     *
     * @throws IllegalStateException if the Transaction is not active anymore.
     *                               todo: exceptions
     */
    void commit();

    /**
     * Aborts the transaction (other transaction will never see the changes made under
     * this transaction). If this Transaction already is aborted, this call is ignored.
     *
     * @throws IllegalStateException if this Transaction already is committed.
     *                               todo: exceptions
     */
    void abort();

    /**
     * Abort and retries this Transaction. Abort and retry is useful for the retry
     * functionality. This method probably is not going to be called by users, but
     * by the TransactionTemplate.
     *
     * @return
     * @throws InterruptedException
     * @throws org.multiverse.api.exceptions.NoProgressPossibleException
     *                              todo: exception.
     */
    Transaction abortAndRetry() throws InterruptedException;

    /**
     * Returns an instance. If originator is null, null is returned. A Transaction will return
     * the same instance every time of a read is done for some originator.
     * <p/>
     * All changes made on read objects will be persisted when the transaction commits.
     *
     * @param originator the originator of this instance to look for.
     * @param <T>        the type of the instance to return.
     * @return the instance, or null if originator is null.
     *         todo: exceptions; what happens when value is not found.
     */
    <T> T read(Originator<T> originator);

    /**
     * Returns an instance
     * <p/>
     * This method should only be called if an object itself is responsible for some kind of
     * dependency. A reread of the same originator doesn't have to lead to the same instance
     * being returned. This helps to prevent a lot of administration on the readset.
     * <p/>
     * Changes made on objects that are not reachable from other objects that are reachable
     * from the transaction, could be ignored.
     *
     * @param originator the Originator of the instance to read
     * @param <T>        the type of the instance to read.
     * @return the instance or null if originator is nu.
     * @throws IllegalStateException if the transaction is not active anymore.
     */
    <T> T readUnmanaged(Originator<T> originator);

    /**
     * Reads a lazy reference to a managed instance.
     * <p/>
     * All changes made on read objects will be persisted when the transaction commits.
     *
     * @param originator the Originator of the instance to read
     * @param <T>        the type of the instance to read
     * @return the lazy reference to the instance, or null if originator is null.
     * @throws IllegalStateException if the transaction is not active anymore.
     */
    <T> LazyReference<T> readLazy(Originator<T> originator);

    /**
     * Reads an lazy reference to an unmanaged instance.
     * <p/>
     * Changes made on objects that are not reachable from other objects that are reachable
     * from the transaction, could be ignored.
     *
     * @param originator the Originator of the instance to read.
     * @param <T>        the type of the instance to read
     * @return the lazy reference, or null if originator is null.
     * @throws IllegalStateException if the transaction is not active anymore.
     */
    <T> LazyReference<T> readLazyAndUnmanaged(Originator<T> originator);

    /**
     * Attaches the object to this session. Objects that are reachable from the Transaction (attached, read
     * reachable from attached or read object) will be persisted when the Transaction commits. So you only need
     * to attach the root of some structure (no need to attach each leaf seperately).
     * <p/>
     * Multiple attaches of the same object are ignored.
     * An attach of a read object, is ignored.
     *
     * @param obj the object to attach.
     * @param <T> the type of the Object to attach.
     * @return
     * @throws IllegalArgumentException if object is not something that can be attached to this
     *                                  Transaction.
     * @throws NullPointerException     if obj is null.
     * @throws IllegalStateException    if the transaction is not active anymore.
     */
    <T> Originator<T> attach(T obj);

    /**
     * Gets the id of this Transaction. This method should be threadsafe to use.
     *
     * @return the id of this Transaction.
     */
    TransactionId getId();

    /**
     * Gets the state of this Transaction. This method should be threadsafe to use.
     *
     * @return the TransactionState.
     */
    TransactionState getState();

    /**
     * Sets the description that describes this Transaction (so you can add your own stuff here).
     *
     * @param description the description
     */
    void setDescription(String description);

    /**
     * Sets the description that describes this Transaction (so you can add your own stuff here).
     *
     * @return the description
     */
    String getDescription();
}
