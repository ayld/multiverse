package org.multiverse.stms;

import org.multiverse.utils.clock.Clock;
import org.multiverse.utils.clock.StrictClock;
import org.multiverse.utils.commitlock.GenericCommitLockPolicy;

/**
 * @author Peter Veentjer
 */
public class AbstractTransactionImpl extends AbstractTransaction {

    public AbstractTransactionImpl() {
        super("", new StrictClock(), GenericCommitLockPolicy.FAIL_FAST);
        init();
    }

    public AbstractTransactionImpl(Clock clock) {
        super("", clock, GenericCommitLockPolicy.FAIL_FAST);
        init();
    }
}
