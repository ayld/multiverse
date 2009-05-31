package org.multiverse.multiversionedstm;

import org.multiverse.api.Handle;
import org.multiverse.api.TransactionId;
import org.multiverse.util.ListenerNode;
import org.multiverse.util.RetryCounter;
import org.multiverse.util.latches.Latch;

public interface MultiversionedHandle<T> extends Handle<T> {

    /**
     * Tries to acquire the lock for writing and also does the detection for write conflicts.
     * <p/>
     *
     * @param committingTransactionId the TransactionId of the Transaction that wants to commit.
     * @param maximumVersion          the maximum version of the committed dehydrated. If the version of the current
     *                                committed state, is newer than maximum version, another transaction has committed and this
     *                                transaction should be retried.
     * @param retryCounter
     * @throws org.multiverse.api.exceptions.WriteConflictException
     *          if another transaction committed after the current
     *          transaction began and before the current transaction committed.
     * @throws org.multiverse.api.exceptions.StarvationException
     *          if the transaction was starved for acquiring the lock.
     */
    void tryAcquireWriteLockAndDetectForConflicts(TransactionId committingTransactionId, long maximumVersion, RetryCounter retryCounter);

    /**
     * Writes the stuff and releases the lock. A write only should be done when the lock for writing has
     * been acquired.
     * <p/>
     *
     * @param committingTransactionId the TransactionId of the Transaction  that wants to commit.
     * @param dematerialized          the stuff to write.
     * @return the head ListenerNode ofthe listeners to notify, could be null.
     */
    ListenerNode writeAndReleaseWriteLock(TransactionId committingTransactionId, DematerializedObject dematerialized,
                                          long dematerializedVersion);

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

    DematerializedObject tryRead(long maximumVersion, RetryCounter retryCounter);

    DematerializedObject tryGetLastCommitted(RetryCounter retryCounter);
}
