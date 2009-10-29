package org.multiverse.stms.alpha;

import org.multiverse.api.Transaction;
import static org.multiverse.stms.alpha.AlphaStmUtils.toAtomicObjectString;
import org.multiverse.utils.clock.Clock;
import org.multiverse.utils.commitlock.CommitLockPolicy;
import org.multiverse.utils.profiling.ProfileRepository;

import static java.lang.String.format;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A logging version of the {@link org.multiverse.stms.alpha.UpdateAlphaTransaction}.
 *
 * @author Peter Veentjer.
 */
public class LoggingUpdateAlphaTransaction extends UpdateAlphaTransaction {

    private final static Logger logger = Logger.getLogger(UpdateAlphaTransaction.class.getName());

    private final long logId;
    private final Level level;

    public LoggingUpdateAlphaTransaction(String familyName, ProfileRepository profiler, Clock clock,
                                         CommitLockPolicy writeSetLockPolicy, long logId, Level level) {
        super(familyName, profiler, clock, writeSetLockPolicy);
        this.logId = logId;
        this.level = level;

        if (logger.isLoggable(level)) {
            logger.log(level, format("%s started", toLogString()));
        }
    }


    private String toLogString() {
        return format("UpdateTransaction '%s-%s' with readversion '%s' ", getFamilyName(), logId, readVersion);
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
                    String msg = format("%s attachNew %s",
                            toLogString(), toAtomicObjectString(tranlocal.getAtomicObject()));
                    logger.log(level, msg);
                } else {
                    String msg = format("%s attachNew %s failed",
                            toLogString(), toAtomicObjectString(tranlocal.getAtomicObject()));
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
                    String msg = format("%s load %s", toLogString(), toAtomicObjectString(atomicObject));
                    logger.log(level, msg);
                } else {
                    String msg = format("%s load %s failed", toLogString(), toAtomicObjectString(atomicObject));
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
                    logger.log(level, format("%s committed with version %s", toLogString(), version));
                } else {
                    logger.log(level, format("%s aborted", toLogString()));
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
                    logger.log(level, format("%s aborted", toLogString()));
                } else {
                    logger.log(level, format("%s abort failed", toLogString()));
                }
            }
        }
    }

    @Override
    public Transaction restart() {
        if (!logger.isLoggable(level)) {
            return super.restart();
        } else {
            boolean success = false;
            String oldLogString = toLogString();
            try {
                Transaction t = super.restart();
                success = true;
                return t;
            } finally {
                if (success) {
                    logger.log(level, format("%s restart to readversion %s", oldLogString, readVersion));
                } else {
                    logger.log(level, format("%s restart failed", oldLogString));
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
                    logger.log(level, format("%s abortedAndRetry successfully", toLogString()));
                } else {
                    logger.log(level, format("%s abortedAndRetry failed", toLogString()));
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
                    logger.log(level, format("%s deferredExecute %s", toLogString(), task));
                } else {
                    logger.log(level, format("%s deferredExecute %s failed", toLogString(), task));
                }
            }
        }
    }

    @Override
    public void compensatingExecute(Runnable task) {
        if (!logger.isLoggable(level)) {
            super.compensatingExecute(task);
        } else {
            boolean success = false;
            try {
                super.compensatingExecute(task);
                success = true;
            } finally {
                if (success) {
                    logger.log(level, format("%s compensatingExecute %s", toLogString(), task));
                } else {
                    logger.log(level, format("%s compensatingExecute %s failed", toLogString(), task));
                }
            }
        }
    }

    @Override
    public String toString() {
        return format("UpdateTransaction %s-%s", getFamilyName(), logId);
    }
}
