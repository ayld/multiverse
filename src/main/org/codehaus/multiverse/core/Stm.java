package org.codehaus.multiverse.core;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A Software Transactional Memory is a piece of memory where you can create transaction (just like databases) to
 * provide the ACI (Atomic, Consistent, Isolated) properties. So changes are atomic (they make it into the stm or
 * not, not partially. etc etc
 *
 * @author Peter Veentjer.
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
    T startRetriedTransaction(T predecessor) throws InterruptedException;

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
    T tryStartRetriedTransaction(T predecessor, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException;
}
