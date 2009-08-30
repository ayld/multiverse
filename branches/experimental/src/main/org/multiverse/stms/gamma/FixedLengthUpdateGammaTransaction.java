package org.multiverse.stms.gamma;

import org.multiverse.api.exceptions.WriteConflictException;
import org.multiverse.utils.TodoException;
import org.multiverse.utils.commitlock.CommitLockPolicy;
import org.multiverse.utils.profiling.Profiler;
import org.multiverse.utils.spinning.SpinPolicy;

import java.util.concurrent.atomic.AtomicLong;

public class FixedLengthUpdateGammaTransaction extends AbstractGammaUpdateTransaction {

    private GammaTranlocal[] tranlocals;

    private final SpinPolicy spinPolicy;

    private int firstFreeIndex = 0;

    public FixedLengthUpdateGammaTransaction(String familyName, AtomicLong clock, CommitLockPolicy lockPolicy, SpinPolicy spinPolicy, Profiler profiler, int length) {
        super(familyName, clock, lockPolicy, profiler);
        this.spinPolicy = spinPolicy;
        this.tranlocals = new GammaTranlocal[length];
        init();
    }

    @Override
    protected void onInit() {
        firstFreeIndex = 0;
    }

    @Override
    protected GammaTranlocal onPrivatize(GammaAtomicObject atomicObject) {
        if (atomicObject == null) {
            return null;
        } else {
            GammaTranlocal tranlocal = find(atomicObject);

            if (tranlocal == null) {
                if (firstFreeIndex == tranlocals.length) {
                    throw new TodoException();
                } else {
                    tranlocal = atomicObject.privatize(readVersion, spinPolicy);
                    tranlocals[firstFreeIndex] = tranlocal;
                    firstFreeIndex++;
                }
            }

            return tranlocal;
        }
    }

    private GammaTranlocal find(GammaAtomicObject atomicObject) {
        for (int k = 0; k < firstFreeIndex; k++) {
            GammaTranlocal tranlocal = tranlocals[k];
            if (tranlocal.getAtomicObject() == atomicObject) {
                return tranlocal;
            }
        }

        return null;
    }

    private boolean contains(GammaTranlocal tranlocal) {
        for (int k = 0; k < firstFreeIndex; k++) {
            if (tranlocals[k] == tranlocal) {
                return true;
            }
        }

        return false;
    }

    private void add(GammaTranlocal tranlocal) {
        if (contains(tranlocal)) {
            return;
        }

        if (firstFreeIndex == tranlocals.length) {
            throw new TodoException();
        }

        tranlocals[firstFreeIndex] = tranlocal;
        firstFreeIndex++;
    }

    @Override
    protected void onAttachAsNew(GammaTranlocal tranlocal) {
        if (tranlocal == null) {
            throw new NullPointerException();
        }

        add(tranlocal);
    }

    protected GammaTranlocal[] createWriteSet() {
        GammaTranlocal[] writeSet = null;

        int writeSetIndex = 0;
        int skipped = 0;
        for (int k = 0; k < firstFreeIndex; k++) {
            GammaTranlocal tranlocal = tranlocals[k];
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
                        writeSet = new GammaTranlocal[tranlocals.length - skipped];
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


        return writeSet;
    }

    @Override
    protected void onAbort() {
        firstFreeIndex = 0;
    }
}
