package org.multiverse.stms.alpha;

import org.multiverse.api.Stm;
import org.multiverse.utils.commitlock.CommitLockPolicy;
import org.multiverse.utils.profiling.Profiler;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Default {@link org.multiverse.api.Stm} implementation.
 * <p/>
 * Statistics:
 * This implementation can use {@link AlphaStmStatistics}. This choice needs to be made
 * when the TL2Stm is constructed, so that the JIT can remove calls to the statistics completely if
 * a null value is passed. The JIT is able to completely remove the following:
 * <pre>
 * if(statistics!=null){
 *      statistics.incSomeCounter();
 * }
 * </pre>
 * So if you are not using the statistics, you don't need to pay for it.
 * <p/>
 * The logging can be completely removed by the JIT if the loggingPossible flag is set to false.
 * No additional checks are done.. so you don't need to pay the price for it if you don't use it.
 *
 * @author Peter Veentjer.
 */
public final class AlphaStm implements Stm {

    private final AtomicLong clock = new AtomicLong();

    private final Profiler profiler;

    private final boolean loggingPossible;

    private final AtomicLong logIdGenerator;

    private final CommitLockPolicy lockPolicy;

    /**
     * Creates a new AlphaStm that keeps track of statistics and where logging is possible.
     */
    public AlphaStm() {
        this(AlphaStmConfig.createDebugConfig());
    }

    /**
     * Creates a new AlphaStm with the provided configuration.
     *
     * @param config the provided config.
     * @throws NullPointerException  if config is null.
     * @throws IllegalStateException if the provided config is invalid.
     */
    public AlphaStm(AlphaStmConfig config) {
        if (config == null) {
            throw new NullPointerException();
        }

        config.ensureValid();

        this.profiler = config.profiler;
        this.loggingPossible = config.loggingPossible;
        this.logIdGenerator = loggingPossible ? new AtomicLong() : null;
        this.lockPolicy = config.commitLockPolicy;
    }

    /**
     * Returns the current WriteSetLockPolicy. Returned value will never be null.
     *
     * @return the current WriteSetLockPolicy.
     */
    public CommitLockPolicy getAtomicObjectLockPolicy() {
        return lockPolicy;
    }

    /**
     * Returns the DefaultStmStatistics or null if the Stm is running without statistics.
     *
     * @return return the TL2StmStatistics.
     */
    public Profiler getProfiler() {
        return profiler;
    }

    @Override
    public AlphaTransaction startUpdateTransaction(String familyName) {
        if (loggingPossible) {
            return new LoggingUpdateAlphaTransaction(
                    familyName, profiler, clock, lockPolicy, logIdGenerator.incrementAndGet());
        } else {
            return new UpdateAlphaTransaction(familyName, profiler, clock, lockPolicy);
        }
    }

    @Override
    public AlphaTransaction startReadOnlyTransaction(String familyName) {
        if (loggingPossible) {
            return new LoggingReadonlyAlphaTransaction(
                    familyName, profiler, clock, logIdGenerator.incrementAndGet());
        } else {
            return new ReadonlyAlphaTransaction(familyName, profiler, clock);
        }
    }

    @Override
    public long getClockVersion() {
        return clock.get();
    }
}
