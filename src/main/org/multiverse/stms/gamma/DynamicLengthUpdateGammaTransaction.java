package org.multiverse.stms.gamma;

import org.multiverse.api.exceptions.PanicError;
import org.multiverse.api.exceptions.WriteConflictException;
import org.multiverse.utils.commitlock.CommitLockPolicy;
import org.multiverse.utils.profiling.Profiler;
import org.multiverse.utils.spinning.SpinPolicy;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public final class DynamicLengthUpdateGammaTransaction extends AbstractGammaUpdateTransaction {

    private Map<GammaAtomicObject, GammaTranlocal> readwriteSet = new IdentityHashMap<GammaAtomicObject, GammaTranlocal>(1);

    private final SpinPolicy spinPolicy;

    public DynamicLengthUpdateGammaTransaction(String familyName, AtomicLong clock, CommitLockPolicy lockPolicy, SpinPolicy spinPolicy, Profiler profiler) {
        super(familyName, clock, lockPolicy, profiler);
        this.spinPolicy = spinPolicy;
        init();
    }

    @Override
    protected void onInit() {
        readwriteSet.clear();
    }

    @Override
    protected GammaTranlocal onPrivatize(GammaAtomicObject atomicObject) {
        if (atomicObject == null) {
            return null;
        } else {
            GammaTranlocal tranlocal = readwriteSet.get(atomicObject);
            if (tranlocal == null) {
                tranlocal = atomicObject.privatize(readVersion, spinPolicy);
                readwriteSet.put(atomicObject, tranlocal);
            }

            return tranlocal;
        }
    }

    @Override
    protected void onAttachAsNew(GammaTranlocal tranlocal) {
        if (tranlocal == null) {
            throw new NullPointerException();
        }

        GammaAtomicObject atomicObject = tranlocal.getAtomicObject();
        GammaTranlocal existing = readwriteSet.put(atomicObject, tranlocal);
        if (existing != null && existing != tranlocal) {
            throw new PanicError();
        }
    }

    protected GammaTranlocal[] createWriteSet() {
        GammaTranlocal[] writeSet = null;

        if (!readwriteSet.isEmpty()) {
            int writeSetIndex = 0;
            int skipped = 0;
            for (Map.Entry<GammaAtomicObject, GammaTranlocal> entry : readwriteSet.entrySet()) {
                GammaTranlocal tranlocal = entry.getValue();
                switch (tranlocal.getDirtinessStatus()) {
                    case clean:
                        skipped++;
                        break;
                    case committed:
                        skipped++;
                        break;
                    case fresh:
                        //fall through
                    case dirty:
                        if (writeSet == null) {
                            writeSet = new GammaTranlocal[readwriteSet.size() - skipped];
                        }
                        writeSet[writeSetIndex] = tranlocal;
                        writeSetIndex++;
                        break;
                    case conflict:
                        throw WriteConflictException.create();
                    default:
                        throw new IllegalStateException();
                }
            }

        }

        return writeSet;
    }

    @Override
    protected void onAbort() {
        readwriteSet.clear();
    }
}
