package org.multiverse.stms.beta;

import org.multiverse.utils.commitlock.CommitLockPolicy;
import org.multiverse.utils.commitlock.GenericCommitLockPolicy;

public class BetaStmConfig {

    public CommitLockPolicy lockPolicy = GenericCommitLockPolicy.FAIL_FAST_BUT_RETRY;

    public void validate() {
        if (lockPolicy == null) {
            throw new RuntimeException("lockPolicy can't be null");
        }
    }
}
