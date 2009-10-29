package org.multiverse.stms.alpha;

import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.LoadUncommittedException;
import org.multiverse.api.exceptions.ReadonlyException;
import org.multiverse.stms.AbstractTransaction;
import static org.multiverse.stms.alpha.AlphaStmUtils.getLoadUncommittedMessage;
import static org.multiverse.stms.alpha.AlphaStmUtils.toAtomicObjectString;
import org.multiverse.utils.clock.Clock;
import org.multiverse.utils.profiling.ProfileRepository;

import static java.lang.String.format;

/**
 * A readonly {@link org.multiverse.api.Transaction} implementation.
 * <p/>
 * Unlike the {@link UpdateAlphaTransaction} a readonly transaction doesn't need track
 * any reads done. This has the advantage that a readonly transaction consumes a lot
 * less resources (so no collection needed to track all the reads) and commits are also a lot
 * quicker (no dirtyness checking).
 * <p/>
 * A disadvantage of not tracking reads, is that the retry/orelse functionality is not available
 * in reaodnly transactions because the transaction has no clue which objects were loaded. So it
 * also has no clue about the objects to listen to on a retry.
 * <p/>
 * Although readonly transactions are isolated from update transactions from a correctness point of
 * view, from a practical point of view a readonly transaction could be obstructed by an update transaction:
 * <p/>
 * in the following scenario, the <u>second</u> load will fail with a {@code LoadTooOldVersionException}:
 * <p/>
 * <pre>
 * T1 (ro):     |--load_X-----load_X--|
 * T2 (up): |---write_X----|
 * </pre>
 * In the future a version history will be added for previous committed data. So the chance that a old version is
 * not available is going to decrease.
 *
 * @author Peter Veentjer.
 */
public class ReadonlyAlphaTransaction extends AbstractTransaction implements AlphaTransaction {
    private final ProfileRepository profiler;

    public ReadonlyAlphaTransaction(String familyName, ProfileRepository profiler, Clock clock) {
        super(familyName, clock, null);
        this.profiler = profiler;

        init();
    }

    protected void onInit() {
        if (profiler != null) {
            profiler.incCounter("readonlytransaction.started.count", getFamilyName());
        }
    }

    @Override
    public AlphaTranlocal load(AlphaAtomicObject atomicObject) {
        switch (status) {
            case active:
                if (atomicObject == null) {
                    return null;
                }

                AlphaTranlocal result = atomicObject.load(readVersion);
                if (result == null) {
                    throw new LoadUncommittedException(getLoadUncommittedMessage(atomicObject));
                }
                return result;
            case committed: {
                String msg = format("Can't load atomicObject '%s' from already committed transaction '%s'.",
                        toAtomicObjectString(atomicObject), familyName);
                throw new DeadTransactionException(msg);
            }
            case aborted: {
                String msg = format("Can't load atomicObject '%s' from already aborted transaction '%s'.",
                        toAtomicObjectString(atomicObject), familyName);
                throw new DeadTransactionException(msg);
            }
            default:
                throw new RuntimeException();
        }
    }

    @Override
    protected long onCommit() {
        long value = super.onCommit();
        if (profiler != null) {
            profiler.incCounter("readonlytransaction.committed.count", getFamilyName());
        }
        return value;
    }

    @Override
    public void attachNew(AlphaTranlocal tranlocal) {
        String msg = format("Can't attach atomicobject '%s' to readonly transaction '%s'.",
                toAtomicObjectString(tranlocal), familyName);
        throw new ReadonlyException(msg);
    }

    @Override
    public boolean isAttached(AlphaAtomicObject atomicObject) {
        return false;
    }

    @Override
    protected void onAbort() {
        super.onAbort();

        if (profiler != null) {
            profiler.incCounter("readonlytransaction.aborted.count", getFamilyName());
        }
    }

    @Override
    public void onAbortAndRetry() {
        throw new ReadonlyException();
    }
}