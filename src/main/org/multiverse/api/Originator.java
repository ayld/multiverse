package org.multiverse.api;

import org.multiverse.multiversionedstm.DematerializedObject;
import org.multiverse.util.RetryCounter;
import org.multiverse.util.latches.Latch;

/**
 * An Originator has 2 purposes:
 * <ol>
 * <li>identifying objects</li>
 * <li>storing the state of objects</li>
 * </ol>
 *
 * @author Peter Veentjer.
 * @param <T>
 */
public interface Originator<T> {

    /**
     * Tries to acquire the lock for writing.
     * <p/>
     * todo: is there any reason to exit with a error and return value?
     *
     * @param committingTransactionId the TransactionId of the Transaction that wants to commit.
     * @param maximumVersion          the maximum version of the committed dehydrated. If the version of the current
     *                                committed state, is newer than maximum version, another transaction has committed and this
     *                                transaction should be retried.
     * @param retryCounter
     * @return true if was a success, false if it was a failure.
     * @throws org.multiverse.api.exceptions.WriteConflictException
     *          if another transaction committed after the current
     *          transaction began and before the current transaction committed.
     */
    boolean tryAcquireLockForWriting(TransactionId committingTransactionId, long maximumVersion, RetryCounter retryCounter);

    /**
     * Writes the stuff and releases the lock. A write only should be done when the lock for writing has
     * been acquired.
     *
     * @param committingTransactionId the TransactionId of the Transaction  that wants to commit.
     * @param dematerialized          the stuff to write.
     */
    void writeAndReleaseLock(TransactionId committingTransactionId, DematerializedObject dematerialized, long dematerializedVersion);

    /**
     * Releases the lock for writing. If a transaction can't acquire all locks it needs for writing,
     * the locks should be released so that other transaction can do a write. That is where this method
     * is for.
     * <p/>
     * This method should succeed, so no TryCounter needed.
     *
     * @param expected the TransactionId of the Transaction that wants to release the lock.
     * @throws IllegalArgumentException if the expected TransactionId is not the same as the found TransactionId.
     */
    void releaseLockForWriting(TransactionId expected);

    boolean tryAddLatch(Latch listener, long minimalVersion, RetryCounter retryCounter);

    DematerializedObject tryGetDehydrated(long maximumVersion, RetryCounter retryCounter);

    DematerializedObject tryGetLastCommitted(RetryCounter retryCounter);
}
