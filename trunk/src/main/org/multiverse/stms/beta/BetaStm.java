package org.multiverse.stms.beta;

import org.multiverse.api.Stm;
import org.multiverse.utils.TodoException;
import org.multiverse.utils.commitlock.CommitLockPolicy;

import java.util.concurrent.atomic.AtomicLong;

/**
 * The {@link BetaStm} is an {@link Stm} implementation that is completely optimized
 * for ref based STM. It doesn't support advanced features like retry/orelse and is
 * optimized for performance/memory usage.
 * <p/>
 * The betastm is:
 * - an stm with object granularity
 * - no support for pessimistic locking
 * - commit time locking*
 * - no support for condition variables (retry/orelse)
 * - invisible reads/writes (defered updates)
 * - provides the oracle version of serialized behavior (so not 100% correct)
 * - flattened transactions are supported, true nested transactions not.
 * - concurrency control: like TL2; so major part optimistic, pessimistic when it commits
 * - conflict detection: late; when the transaction commits.
 * - lock free: no, because locks are acquired when the transaction commits. If the transaction somehow is
 * stalled, the locks are released and other transaction that want to update the same objects are going
 * to block/fail.
 * - isolation: strong; it is not possible (unless you are hacking) to access fields of an object (tranlocal)
 * outside of a transaction.
 *
 * @author Peter Veentjer.
 */
public final class BetaStm implements Stm {

    private final AtomicLong clock = new AtomicLong();

    private final CommitLockPolicy lockPolicy;

    public BetaStm() {
        this(new BetaStmConfig());
    }

    public BetaStm(BetaStmConfig config) {
        if (config == null) {
            throw new NullPointerException();
        }

        lockPolicy = config.lockPolicy;
    }

    @Override
    public long getClockVersion() {
        return clock.get();
    }

    @Override
    public BetaTransaction startUpdateTransaction(String familyName) {
        return new UpdateBetaTransaction(familyName, clock, lockPolicy);
    }

    @Override
    public BetaTransaction startReadOnlyTransaction(String familyName) {
        return new ReadonlyBetaTransaction(familyName, clock);
    }

    @Override
    public BetaTransaction startFlashbackTransaction(String familyName, long readVersion) {
        throw new TodoException(familyName);
    }
}
