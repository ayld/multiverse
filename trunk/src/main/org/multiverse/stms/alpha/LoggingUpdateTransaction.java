package org.multiverse.stms.alpha;

import org.multiverse.utils.atomicobjectlocks.AtomicObjectLockPolicy;

import static java.lang.String.format;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggingUpdateTransaction extends UpdateTransaction {

    private static Logger logger = Logger.getLogger(UpdateTransaction.class.getName());

    public final static AtomicLong logIdGenerator = new AtomicLong();

    private final long logId = logIdGenerator.getAndIncrement();

    public LoggingUpdateTransaction(AlphaStmStatistics statistics, AtomicLong clock, AtomicObjectLockPolicy writeSetLockPolicy) {
        super(statistics, clock, writeSetLockPolicy);

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
    public void abortAndRetry() {
        if (!logger.isLoggable(Level.FINE)) {
            super.abortAndRetry();
        } else {
            boolean success = false;
            try {
                super.abortAndRetry();
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
    public void executePostCommit(Runnable task) {
        if (!logger.isLoggable(Level.FINER)) {
            super.executePostCommit(task);
        } else {
            boolean success = false;
            try {
                super.executePostCommit(task);
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
