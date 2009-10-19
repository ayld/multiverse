package org.multiverse.stms.alpha;

import org.multiverse.utils.clock.Clock;
import org.multiverse.utils.commitlock.CommitLockPolicy;
import org.multiverse.utils.profiling.ProfileRepository;

import static java.lang.String.format;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggingUpdateAlphaTransaction extends UpdateAlphaTransaction {

    private final static Logger logger = Logger.getLogger(UpdateAlphaTransaction.class.getName());
    
    private final long logId;
    private final Level level;

    public LoggingUpdateAlphaTransaction(String familyName, ProfileRepository profiler, Clock clock, CommitLockPolicy writeSetLockPolicy, long logId, Level level) {
        super(familyName, profiler, clock, writeSetLockPolicy);
        this.logId = logId;
        this.level = level;

        if (logger.isLoggable(level)) {
            logger.log(level, format("UpdateTransaction-%s with readversion %s started", logId, getReadVersion()));
        }
    }

    @Override
    public void attachNew(AlphaTranlocal tranlocal) {
        if (!logger.isLoggable(Level.FINE)) {
            super.attachNew(tranlocal);
        } else {
            boolean success = false;
            try {
                super.attachNew(tranlocal);
                success = true;
            } finally {
                if (success) {
                    String msg = format("UpdateTransaction-%s attachNew %s", logId, System.identityHashCode(tranlocal));
                    logger.log(level, msg);
                } else {
                    String msg = format("UpdateTransaction-%s attachNew %s failed", logId,System.identityHashCode(tranlocal));
                    logger.log(level, msg);
                }
            }
        }
    }

    @Override
    public AlphaTranlocal load(AlphaAtomicObject atomicObject) {
        if (!logger.isLoggable(level)) {
            return super.load(atomicObject);
        } else {
            boolean success = false;
            try {
                AlphaTranlocal tranlocal = super.load(atomicObject);
                success = true;
                return tranlocal;
            } finally {
                if (success) {
                    String msg = format("UpdateTransaction-%s load %s", logId, System.identityHashCode(atomicObject));
                    logger.log(level, msg);
                } else {
                    String msg = format("UpdateTransaction-%s load %s failed", logId,System.identityHashCode(atomicObject));
                    logger.log(level, msg);
                }
            }
        }
    }

    @Override
    public long commit() {
        if (!logger.isLoggable(level)) {
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
                    logger.log(level, format("UpdateTransaction-%s committed with version %s", logId, version));
                } else {
                    logger.log(level, format("UpdateTransaction-%s aborted", logId));
                }
            }
        }
    }

    @Override
    public void abort() {
        if (!logger.isLoggable(level)) {
            super.abort();
        } else {
            boolean success = false;
            try {
                super.abort();
                success = true;
            } finally {
                if (success) {
                    logger.log(level, format("UpdateTransaction-%s aborted", logId));
                } else {
                    logger.log(level, format("UpdateTransaction-%s abort failed", logId));
                }
            }
        }
    }

    @Override
    public void reset() {
        if (!logger.isLoggable(level)) {
            super.reset();
        } else {
            boolean success = false;
            try {
                super.reset();
                success = true;
            } finally {
                if (success) {
                    logger.log(level, format("UpdateTransaction-%s reset", logId));
                } else {
                    logger.log(level, format("UpdateTransaction-%s reset failed", logId));
                }
            }
        }
    }

    @Override
    public void abortAndWaitForRetry() {
        if (!logger.isLoggable(level)) {
            super.abortAndWaitForRetry();
        } else {
            boolean success = false;
            try {
                super.abortAndWaitForRetry();
                success = true;
            } finally {
                if (success) {
                    logger.log(level, format("UpdateTransaction-%s abortedAndRetry successfully", logId));
                } else {
                    logger.log(level, format("UpdateTransaction-%s abortedAndRetry failed", logId));
                }
            }
        }
    }

    @Override
    public void deferredExecute(Runnable task) {
        if (!logger.isLoggable(level)) {
            super.deferredExecute(task);
        } else {
            boolean success = false;
            try {
                super.deferredExecute(task);
                success = true;
            } finally {
                if (success) {
                    logger.log(level, format("UpdateTransaction-%s executePostCommit %s", logId, task));
                } else {
                    logger.log(level, format("UpdateTransaction-%s executePostCommit %s failed", logId, task));
                }
            }
        }
    }

    @Override
    public String toString() {
        return "UpdateTransaction" + logId;
    }
}
