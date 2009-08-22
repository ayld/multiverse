package org.multiverse.stms.alpha;

import org.multiverse.api.Stm;
import org.multiverse.api.Transaction;
import org.multiverse.utils.atomicobjectlocks.AtomicObjectLockPolicy;
import org.multiverse.utils.atomicobjectlocks.GenericAtomicObjectLockPolicy;

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

    private final AlphaStmStatistics statistics;

    private final boolean loggingPossible;

    private volatile AtomicObjectLockPolicy lockPolicy;

    /**
     * Creates a new AlphaStm that keeps track of statistics and where logging is possible.
     */
    public AlphaStm() {
        this(new AlphaStmStatistics(), GenericAtomicObjectLockPolicy.FAIL_FAST_BUT_RETRY, true);
    }

    /**
     * Creates a new AlphaStm with the given statistics. If no statistics is given, the JIT is able
     * to remove all calls to the statistics, so we don't need to pay to price if we don't use it.
     * It also means that the value can't be changed after construction.
     *
     * @param statistics      the TL2StmStatistics to use, or null of none should be used.
     * @param lockPolicy      the AtomicObjectLockPolicy used to acquire the locks.
     * @param loggingPossible if logging to java.util.logging is possible. If logging is disabled, a different
     *                        transaction class will be used that doesn't cause any logging overhead.
     * @throws NullPointerException if lockPolicy is null.
     */
    public AlphaStm(AlphaStmStatistics statistics, AtomicObjectLockPolicy lockPolicy, boolean loggingPossible) {
        if (lockPolicy == null) {
            throw new NullPointerException();
        } else {
            this.statistics = statistics;
            this.loggingPossible = loggingPossible;
            this.lockPolicy = lockPolicy;
        }
    }

    /**
     * Returns the current WriteSetLockPolicy. Returned value will never be null.
     *
     * @return the current WriteSetLockPolicy.
     */
    public AtomicObjectLockPolicy getAtomicObjectLockPolicy() {
        return lockPolicy;
    }

    /**
     * Sets the new WriteSetLockPolicy.
     *
     * @param newLockPolicy the new WriteSetLockPolicy.
     * @throws NullPointerException if newWriteSetLockPolicy is null.
     */
    public void setAtomicObjectLockPolicy(AtomicObjectLockPolicy newLockPolicy) {
        if (newLockPolicy == null) {
            throw new NullPointerException();
        } else {
            this.lockPolicy = newLockPolicy;
        }
    }

    /**
     * Returns the DefaultStmStatistics or null if the Stm is running without statistics.
     *
     * @return return the TL2StmStatistics.
     */
    public AlphaStmStatistics getStatistics() {
        return statistics;
    }

    @Override
    public Transaction startUpdateTransaction() {
        if (loggingPossible) {
            return new LoggingUpdateTransaction(statistics, clock, lockPolicy);
        } else {
            return new UpdateTransaction(statistics, clock, lockPolicy);
        }
    }

    @Override
    public Transaction startReadOnlyTransaction() {
        return new ReadonlyTransaction(statistics, clock);
    }

    @Override
    public Transaction startFlashbackTransaction(long readVersion) {
        if (readVersion > clock.get()) {
            throw new IllegalArgumentException();
        }

        return new ReadonlyTransaction(statistics, readVersion);
    }

    @Override
    public long getClockVersion() {
        return clock.get();
    }
}
