package org.codehaus.multiverse.api;


/**
 * All operations done on the {@link Stm} are always done through a transaction. The transaction makes sure that
 * all changes are atomic (so all of them enter the stm, or none of them). And the transaction also makes sure that
 * the it is isolated from other transactions. Unlike a database transaction, the changes are not 'durable', so
 * when the power is turned off, all changes are lost. And unlike the database transaction the stm transaction is
 * not responsible for consistency; that is part of the Java code itself.
 * <p/>
 * A Transaction can be compared to a Hibernate Session.
 * <p/>
 * A Transaction is not threadsafe and should not be shared between threads, unless it is safely moved from
 * one thread to another.
 * <p/>
 * Normally a transaction should not be managed manually, but a {@link TransactionTemplate} should be used for that.
 * The following examples are just for information (the template does the start/commit/retry for your).
 * <p/>
 * Example with creating an item:
 * <pre>
 *  Transaction t = stm.startTransaction();
 *  long stackHandle = t.attachAsRoot(new Stack());
 *  t.commit();
 * </pre>
 * The attachAsRoot provides a handle to access the object later. In most cases you only want to have handles of
 * high level objects. As soon as you have obtained the object the handle points to, you can work with normal object
 * references.
 * <p/>
 * And example with only a read:
 * <pre>
 *  Transaction t = stm.startTransaction();
 *  Stack stack = (Stack)t.read(stackHandle);
 *  int size = stack.size();
 *  t.commit();
 * </pre>
 * <p/>
 * An example with an update:
 * <pre>
 *   Transaction t = stm.startTransaction();
 *   Person p = (Person)t.read(personHandle);
 *   person.incAge();
 *   t.commit();
 * </pre>
 * <p/>
 * An example with traversal of objects:
 * <pre>
 *  Transaction t = stm.startTransaction();
 *  Stack s = (Stack)t.read(stackHandle);
 *  s.push(new Person());
 *  t.commit();
 * </pre>
 * <p/>
 * <h3>Don't let transactions dangle around!</h3>.
 * A Transaction should be committed or aborted so that the resources it has collected (for example
 * locks) will be released as soon as possible. If this isn't done, resources could be claimed longer
 * than possible and this could reduce performance. Another reason to end transactions, is that it depends
 * on the stm implemention to abort transactions that can be garbage collected. So don't rely on it and
 * end your transactions!
 *
 * @author Peter Veentjer
 * @see TransactionTemplate
 */
public interface Transaction {

    /**
     * Attaches the object to the Transaction. Multiple calls to the attach method on the same transaction of the
     * same object are ignored. Roots that are not fresh can be attached again (call will be ignored). If other
     * objects can be reached from the object, they are attached as well.
     * <p/>
     * This method is not threadsafe.
     *
     * @param root the object to attach
     * @return the handle of the object. If the transaction is not committed, this address is not valid. The handle
     *         can be used to retrieve objects with the {@link #read(long)} method.
     * @throws NullPointerException     if object is null.
     * @throws IllegalStateException    if this Transaction is not in the started state.
     * @throws IllegalArgumentException if object is not an object that can be stored in this transaction. A specific
     *                                  type for the root, can't be used, because compile time the instrumentation
     *                                  of additional interfaces has not been done.
     */
    long attachAsRoot(Object root);

    /**
     * Reads an object from the stm using this Transaction. Only if the handle is 0, null is returned. The same
     * instance will always be returned.
     * <p/>
     * This method is not threadsafe.
     *
     * @param handle the handle to the object to read.
     * @return the object at the handle
     * @throws org.codehaus.multiverse.api.exceptions.NoSuchObjectException
     *          if the object with the given handle doesn't exist.
     */
    Object read(long handle);

    Object readAndLockOrFail(long handle, LockMode lockMode);

    Object readAndLockOrBlock(long handle, LockMode lockMode);

    /**
     * Returns the status of this Transaction
     * <p/>
     * This method is threadsafe. Value could be stale as soon as it is received.
     *
     * @return the status of this Transaction
     */
    TransactionStatus getStatus();

    /**
     * Commits the changes to STM. If the transaction already is committed, the call is ignored. If an exception
     * is thrown during commit, the transaction will be aborted automatically.
     * <p/>
     * This method is not threadsafe.
     * <p/>
     * An commit can be as cheap as the abort (depends on the implementation)  If you abort a successfull
     * readonly/nolocks transaction it could give the impression that your system is suffering from a lot of aborted transaction
     * if the statistics are monitored somehow. So if there is no reason to abort, just commit; don't
     * try to be clever.
     *
     * @throws IllegalStateException if the transaction is already aborted.
     * @throws org.codehaus.multiverse.api.exceptions.WriteConflictError
     *                               if a write conflict has happened
     */
    void commit();

    /**
     * Aborts the transaction. If changes have been made that become visibile to other threads,
     * they are rolled back as well. Multiple calls on the abort method are ignored.
     * <p/>
     * This method is not threadsafe.
     * <p/>
     * An abort can be very cheap (depends on the implementation). If no resources like locks
     * have been claimed, it would be nothing more than switching a flag. So don't be clever
     * and always commit or abort your transactions.
     *
     * @throws IllegalStateException if the Transaction already has been committed.
     */
    void abort();

    /**
     * Gets the TransactionId that uniquely identifies this Transaction.
     * <p/>
     * This method is threadsafe and can be called no matter the state of the Transaction.
     *
     * @return the TransactionId that uniquely identifies a Transaction
     */
    TransactionId getId();

    /**
     * Gets the PessimisticLock for some stm object. The Lock it not acquired.
     * <p/>
     * No guarantees are made if the same Lock instance is returned for an object, or a different one.
     * But this should not be any concern of the caller.
     * <p/>
     * This method is not threadsafe.
     *
     * @param object the Object to get the PessimisticLock for.
     * @return the PessimisticLock that belongs to the object.
     * @throws NullPointerException     if object is null.
     * @throws IllegalArgumentException if object is not an stm object
     * @throws org.codehaus.multiverse.api.exceptions.NoSuchLockException
     *
     * @throws IllegalStateException    if the thread isn't in the started state anymore.
     */
    PessimisticLock getPessimisticLock(Object object);
}
