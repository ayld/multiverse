package org.multiverse.stms.alpha;

import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.LoadUncommittedException;
import org.multiverse.api.exceptions.ReadonlyException;
import org.multiverse.stms.AbstractTransaction;
import org.multiverse.utils.profiling.ProfileDataRepository;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A readonly {@link org.multiverse.api.Transaction} implementation. Unlike the {@link UpdateAlphaTransaction}
 * a readonly transaction doesn't need track any reads done. This has the advantage that a
 * readonly transaction consumes a lot less resources.
 *
 * @author Peter Veentjer.
 */
public class ReadonlyAlphaTransaction extends AbstractTransaction implements AlphaTransaction {
    private final ProfileDataRepository profiler;

    public ReadonlyAlphaTransaction(String familyName, ProfileDataRepository profiler, AtomicLong clock) {
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
                    throw new LoadUncommittedException();
                }
                return result;
            case committed:
                throw new DeadTransactionException("Can't load from an already committed transaction");
            case aborted:
                throw new DeadTransactionException("Can't load from an already aborted transaction");
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
        throw new ReadonlyException("Can't attach newly created atomicobject to a readonly transaction, " +
                "atomicObject: " + tranlocal.getAtomicObject().getClass());
    }

    @Override
    public boolean isAttached(AlphaAtomicObject atomicObject) {
        throw new ReadonlyException("Can't attach newly created atomicobject to a readonly transaction, " +
                "atomicObject: " + atomicObject.getClass());
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
