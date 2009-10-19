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
            logger.log(level,format("ReadonlyTransaction%s and readversion %s started", logId, getReadVersion()));
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
                    logger.log(level,format("ReadonlyTransaction%s committed with version %s", logId, version));
                } else {
                    logger.log(level,format("ReadonlyTransaction%s aborted", logId));
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
                    logger.log(level,format("ReadonlyTransaction%s aborted", logId));
                } else {
                    logger.log(level,format("ReadonlyTransaction%s abort failed", logId));
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
                    logger.log(level,format("ReadonlyTransaction%s reset", logId));
                } else {
                    logger.log(level,format("ReadonlyTransaction%s reset failed", logId));
                }
            }
        }
    }

    @Override
    public String toString() {
        return "ReadonlyTransaction" + logId;
    }
}
