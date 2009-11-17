package org.multiverse.stms;

import org.multiverse.utils.clock.Clock;
import org.multiverse.utils.clock.StrictClock;

/**
 * @author Peter Veentjer
 */
public class AbstractTransactionImpl extends AbstractTransaction {

    public AbstractTransactionImpl() {
        super("", new StrictClock());
        init();
    }

    public AbstractTransactionImpl(Clock clock) {
        super("", clock);
        init();
    }
}
