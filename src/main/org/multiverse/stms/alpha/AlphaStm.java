package org.multiverse.stms.alpha;

import org.multiverse.api.Stm;
import org.multiverse.api.Transaction;
import org.multiverse.stms.alpha.writeset.FailFastWriteSetLockPolicy;
import org.multiverse.stms.alpha.writeset.WriteSetLockPolicy;

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
 *
 * @author Peter Veentjer.
 */
public final class AlphaStm implements Stm {

    private final AtomicLong clock = new AtomicLong();

    private final AlphaStmStatistics statistics;

    private volatile WriteSetLockPolicy writeSetLockPolicy = new FailFastWriteSetLockPolicy();

    /**
     * Creates a new TL2Stm that keeps track of statistics.
     */
    public AlphaStm() {
        this(new AlphaStmStatistics());
    }

    /**
     * Creates a new TL2Stm with the given statistics. If no statistics is given, the JIT is able
     * to remove all calls to this statistics, so we don't need to pay to price if we don't use it.
     * It also means that the value can't be changed after construction.
     *
     * @param statistics the TL2StmStatistics to use, or null of none should be used.
     */
    public AlphaStm(AlphaStmStatistics statistics) {
        this.statistics = statistics;
    }

    /**
     * Returns the current WriteSetLockPolicy. Returned value will never be null.
     *
     * @return the current WriteSetLockPolicy.
     */
    public WriteSetLockPolicy getAcquireLocksPolicy() {
        return writeSetLockPolicy;
    }

    /**
     * Sets the new WriteSetLockPolicy.
     *
     * @param newWriteSetLockPolicy the new WriteSetLockPolicy.
     * @throws NullPointerException if newWriteSetLockPolicy is null.
     */
    public void setAcquireLocksPolicy(WriteSetLockPolicy newWriteSetLockPolicy) {
        if (newWriteSetLockPolicy == null) {
            throw new NullPointerException();
        }
        this.writeSetLockPolicy = newWriteSetLockPolicy;
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
        return new UpdateTransaction(statistics, clock, writeSetLockPolicy);
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
