package org.multiverse.stms.alpha;

import org.multiverse.api.Tranlocal;
import org.multiverse.api.Transaction;
import org.multiverse.utils.latches.Latch;

/**
 * This is the interface placed on the POJO's that want to participate in the STM.
 *
 * In most cases a user of the library is not going to worry bout this interface. Instrumentation
 * is going to do all that work based on annotations ({@link org.multiverse.api.annotations.AtomicMethod}).
 *
 * @author Peter Veentjer.
 */
public interface AlphaAtomicObject {

    /**
     * Loads the {@link org.multiverse.api.Tranlocal} with a version equal or smaller than readVersion. It is very
     * important for the implementation to not to return a too old version. If this happens, the
     * system could start to suffer from lost updates (not seeing changes you should have seen).
     *
     * @param readVersion the version of the Tranlocal to read.
     * @return the loaded Tranlocal. If nothing is committed, null is returned.
     * @throws org.multiverse.api.exceptions.LoadException
     *          if the system wasn't able to load the Tranlocal.
     */
    Tranlocal load(long readVersion);

    /**
     * Loads the {@link Tranlocal} with the specified version and returns a private copy that can be used
     * for updating transactions.
     * <p/>
     * todo:
     * if you can see that another transaction already did a commit.. no need to continue
     * <p/>
     * todo:
     * what to do if no committed data was found.
     *
     * @param readVersion the readversion of the transaction.
     * @return the loaded Tranlocal.
     * @throws org.multiverse.api.exceptions.LoadException
     *          if the system wasn't able to load the Tranlocal.
     * @throws org.multiverse.api.exceptions.WriteConflictException
     *          if is already able to determine that
     *          a write will never be successful.
     */
    Tranlocal privatize(long readVersion);

    /**
     * Acquires the lock. The lock is only acquired it the lock is free.
     *
     * @param lockOwner the owner of the lock.
     * @return true if the lock was acquired, false otherwise.
     */
    boolean acquireLock(Transaction lockOwner);

    /**
     * Releases the lock. The lock is only released if the current lockowner is equal to the expected
     * lock owner. If the lock is free, this call is ignored. If the lock is owned by a different transaction
     * this call is ignored.
     * <p/>
     * It is important that this method always completes. If it doesn't, it could leave memory
     * in an inconsistent state (locked) and therefor useless.
     *
     * @param expectedLockOwner the expected LockOwner.
     */
    void releaseLock(Transaction expectedLockOwner);

    /**
     * Stores the the content and releases the lock.
     * <p/>
     * It is important that this call only is made when the lock already was acquired.
     *
     * @param tranlocal    the Tranlocal to store.
     * @param writeVersion the version to store the Tranlocal with.
     */
    void storeAndReleaseLock(Tranlocal tranlocal, long writeVersion);

    /**
     * Registers a listener for retrying (the condition variable version for STM's). The Latch is a
     * concurrency structure that can be used to let a thread (transaction) wait for a specific event.
     * In this case we use it to notify the Transaction that the desired update has taken place.
     *
     * @param listener       the Latch to register.
     * @param minimumVersion the minimum version of the data
     * @return true if the listener was registered on a committed object, false otherwise.
     */
    boolean registerRetryListener(Latch listener, long minimumVersion);

    /**
     * @param readVersion the read version of the transaction.
     * @return true if valide, false otherwise.
     * @throws org.multiverse.api.exceptions.LoadException
     *
     */
    boolean validate(long readVersion);

    /**
     * Returns the current owner of the lock, or null if AtomicObject is not locked.
     *
     * @return the current owner, or null if lock is free.
     */
    Transaction getLockOwner();
}
