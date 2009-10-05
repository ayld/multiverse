package org.multiverse.stms.alpha;

import org.multiverse.utils.profiling.ProfileDataRepository;

import static java.lang.String.format;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggingReadonlyAlphaTransaction extends ReadonlyAlphaTransaction {

    private final static Logger logger = Logger.getLogger(UpdateAlphaTransaction.class.getName());

    private final long logId;

    public LoggingReadonlyAlphaTransaction(String familyName, ProfileDataRepository profiler, AtomicLong clock, long logId) {
        super(familyName, profiler, clock);

        this.logId = logId;

        if (logger.isLoggable(Level.FINE)) {
            logger.fine(format("ReadonlyTransaction%s and readversion %s started", logId, getReadVersion()));
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
                    logger.fine(format("ReadonlyTransaction%s committed with version %s", logId, version));
                } else {
                    logger.fine(format("ReadonlyTransaction%s aborted", logId));
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
                    logger.fine(format("ReadonlyTransaction%s aborted", logId));
                } else {
                    logger.fine(format("ReadonlyTransaction%s abort failed", logId));
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
                    logger.fine(format("ReadonlyTransaction%s reset", logId));
                } else {
                    logger.fine(format("ReadonlyTransaction%s reset failed", logId));
                }
            }
        }
    }

    @Override
    public String toString() {
        return "ReadonlyTransaction" + logId;
    }
}
