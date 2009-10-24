package org.multiverse.stms.alpha;

import org.multiverse.utils.clock.Clock;
import org.multiverse.utils.profiling.ProfileRepository;

import static java.lang.String.format;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggingReadonlyAlphaTransaction extends ReadonlyAlphaTransaction {

    private final static Logger logger = Logger.getLogger(UpdateAlphaTransaction.class.getName());

    private final long logId;
    private final Level level;

    public LoggingReadonlyAlphaTransaction(String familyName, ProfileRepository profiler, Clock clock, long logId, Level level) {
        super(familyName, profiler, clock);

        this.logId = logId;
        this.level = level;

        if (logger.isLoggable(level)) {
            logger.log(level, format("%s started", toLogString()));
        }
    }

    private String toLogString() {
        return format("ReadonlyTransaction '%s-%s' with readversion '%s' ", getFamilyName(), logId, readVersion);
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
    public void reset() {
        if (!logger.isLoggable(level)) {
            super.reset();
        } else {
            boolean success = false;
            String oldLogString = toLogString();
            try {
                super.reset();
                success = true;
            } finally {
                if (success) {
                    logger.log(level, format("%s reset to readversion %s", oldLogString, readVersion));
                } else {
                    logger.log(level, format("%s reset failed", oldLogString));
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
        return toLogString();
    }
}
