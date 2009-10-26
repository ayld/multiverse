package org.multiverse.stms.alpha;

import static java.lang.String.format;
import static org.multiverse.stms.alpha.AlphaStmUtils.getLoadUncommittedMessage;
import static org.multiverse.stms.alpha.AlphaStmUtils.toAtomicObjectString;

import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.LoadUncommittedException;
import org.multiverse.api.exceptions.ReadonlyException;
import org.multiverse.stms.AbstractTransaction;
import org.multiverse.utils.clock.Clock;
import org.multiverse.utils.profiling.ProfileRepository;

/**
 * A readonly {@link org.multiverse.api.Transaction} implementation. 
 * <p>
 * Unlike the {@link UpdateAlphaTransaction} a readonly transaction doesn't need track 
 * any reads done. This has the advantage that a readonly transaction consumes a lot 
 * less resources. 
 * <p>
 * It also means that a readonly transactions are not isolated from writes
 * may by concurrent update transactions: in the following scenario, the <u>second</u> load 
 * will fail with a {@code LoadTooOldVersionException}:
 * 
 * <pre>
 * T1 (ro):     |--load_X-----load_X--|
 * T2 (up): |---write_X----|
 * </pre>
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
