package org.codehaus.multiverse;

import org.codehaus.multiverse.transaction.Transaction;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Part of the methods should be moved to a new interface: VersionedStm since these methods are very implementation
 * specific. A Lockbased stm doesn't need the versions.
 */
public interface Stm<T extends Transaction> {

    //todo: add version that allows params like readonly

    /**
     * Starts a new Transaction.
     * <p/>
     * This method is threadsafe.
     *
     * @return the started Transaction.
     */
    T startTransaction();

    /**
     * Starts a new Transaction as soon as the reality the precessor thread saw has changed, or blocks of no
     * change has happened. This method is useful for the 'retry' (stm version of condition variables functionality).
     * <p/>
     * This method is threadsafe.
     *
     * @param predecessor
     * @return the started Transaction
     * @throws InterruptedException if the thread is interrupted.
     * @throws NullPointerException if predecessor is null.
     */
    T startTransaction(Transaction predecessor) throws InterruptedException;

    /**
     * Starts a new Transaction as soon as the reality the precessor thread saw has changed, or blocks of no
     * change has happened. This method is useful for the 'retry' (stm version of condition variables functionality).
     * <p/>
     * This method is threadsafe.
     *
     * @param predecessor
     * @param timeout
     * @param unit
     * @return the started Transaction
     * @throws InterruptedException if the thread is interrupted.
     * @throws TimeoutException     if a timeout occurred.
     * @throws NullPointerException if predecessor or unit is null.
     */
    T tryStartTransaction(Transaction predecessor, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException;
}
