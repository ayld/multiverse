package org.multiverse.stms.alpha;

import org.multiverse.utils.commitlock.CommitLockPolicy;
import org.multiverse.utils.profiling.Profiler;

import static java.lang.String.format;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggingUpdateAlphaTransaction extends UpdateAlphaTransaction {

    private final static Logger logger = Logger.getLogger(UpdateAlphaTransaction.class.getName());

    private final long logId;

    public LoggingUpdateAlphaTransaction(String familyName, Profiler profiler, AtomicLong clock, CommitLockPolicy writeSetLockPolicy, long logId) {
        super(familyName, profiler, clock, writeSetLockPolicy);

        this.logId = logId;

        if (logger.isLoggable(Level.FINE)) {
            logger.fine(format("UpdateTransaction%s and readversion %s started", logId, getReadVersion()));
        }
    }

    @Override
    public long commit() {
        if (!logger.isLoggable(Level.FINE)) {
            return super.commit();
        } else {
            boolean success = false;
            long version = Long.MIN_VALUE;
            try {
                version = super.commit();
                success = true;
                return version;
            } finally {
                if (success) {
                    logger.fine(format("UpdateTransaction%s committed with version %s", logId, version));
                } else {
                    logger.fine(format("UpdateTransaction%s aborted", logId));
                }
            }
        }
    }

    @Override
    public void abort() {
        if (!logger.isLoggable(Level.FINE)) {
            super.abort();
        } else {
            boolean success = false;
            try {
                super.abort();
                success = true;
            } finally {
                if (success) {
                    logger.fine(format("UpdateTransaction%s aborted", logId));
                } else {
                    logger.fine(format("UpdateTransaction%s abort failed", logId));
                }
            }
        }
    }

    @Override
    public void reset() {
        if (!logger.isLoggable(Level.FINE)) {
            super.reset();
        } else {
            boolean success = false;
            try {
                super.reset();
                success = true;
            } finally {
                if (success) {
                    logger.fine(format("UpdateTransaction%s reset", logId));
                } else {
                    logger.fine(format("UpdateTransaction%s reset failed", logId));
                }
            }
        }
    }

    @Override
    public void retry() {
        if (!logger.isLoggable(Level.FINE)) {
            super.retry();
        } else {
            boolean success = false;
            try {
                super.retry();
                success = true;
            } finally {
                if (success) {
                    logger.fine(format("UpdateTransaction%s retried", logId));
                } else {
                    logger.fine(format("UpdateTransaction%s retry failed", logId));
                }
            }
        }
    }

    @Override
    public void abortAndWaitForRetry() {
        if (!logger.isLoggable(Level.FINE)) {
            super.abortAndWaitForRetry();
        } else {
            boolean success = false;
            try {
                super.abortAndWaitForRetry();
                success = true;
            } finally {
                if (success) {
                    logger.fine(format("UpdateTransaction%s abortedAndRetry successfully", logId));
                } else {
                    logger.fine(format("UpdateTransaction%s abortedAndRetry failed", logId));
                }
            }
        }
    }

    @Override
    public void deferredExecute(Runnable task) {
        if (!logger.isLoggable(Level.FINER)) {
            super.deferredExecute(task);
        } else {
            boolean success = false;
            try {
                super.deferredExecute(task);
                success = true;
            } finally {
                if (success) {
                    logger.finer(format("UpdateTransaction%s executePostCommit %s", logId, task));
                } else {
                    logger.finer(format("UpdateTransaction%s executePostCommit %s failed", logId, task));
                }
            }
        }
    }

    @Override
    public String toString() {
        return "UpdateTransaction" + logId;
    }
}
