package org.multiverse.stms.gamma;

import org.multiverse.api.Stm;
import org.multiverse.utils.TodoException;
import org.multiverse.utils.commitlock.CommitLockPolicy;
import org.multiverse.utils.commitlock.GenericCommitLockPolicy;
import org.multiverse.utils.profiling.Profiler;
import org.multiverse.utils.spinning.BoundedSpinPolicy;
import org.multiverse.utils.spinning.SpinPolicy;

import java.util.concurrent.atomic.AtomicLong;

public final class GammaStm implements Stm {

    private final AtomicLong clock = new AtomicLong(0);
    private final CommitLockPolicy commitLockPolicy;
    private final SpinPolicy spinPolicy;
    private final Profiler profiler;

    public GammaStm() {
        this(GenericCommitLockPolicy.FAIL_FAST_BUT_RETRY, BoundedSpinPolicy.INSTANCE, null);
    }

    public GammaStm(CommitLockPolicy commitLockPolicy, SpinPolicy spinPolicy, Profiler profiler) {
        if (commitLockPolicy == null || spinPolicy == null) {
            throw new NullPointerException();
        }
        this.commitLockPolicy = commitLockPolicy;
        this.spinPolicy = spinPolicy;
        this.profiler = profiler;
    }

    @Override
    public long getClockVersion() {
        return clock.get();
    }

    public CommitLockPolicy getCommitLockPolicy() {
        return commitLockPolicy;
    }

    public SpinPolicy getSpinPolicy() {
        return spinPolicy;
    }

    @Override
    public GammaTransaction startUpdateTransaction(String familyName) {
        try {
            return new DynamicLengthUpdateGammaTransaction(familyName, clock, commitLockPolicy, spinPolicy, profiler);
        } finally {
            if (profiler != null) {
                profiler.getCounter(familyName, "updatetransaction.started").incrementAndGet();
            }
        }
    }

    @Override
    public GammaTransaction startReadOnlyTransaction(String familyName) {
        throw new TodoException();
    }

    @Override
    public GammaTransaction startFlashbackTransaction(String familyName, long readVersion) {
        throw new TodoException();
    }
}
