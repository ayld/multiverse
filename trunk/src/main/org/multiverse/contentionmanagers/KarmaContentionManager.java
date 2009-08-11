package org.multiverse.contentionmanagers;

import org.multiverse.api.Transaction;
import org.multiverse.utils.TodoException;

/**
 * Karma: Each transaction keeps track of how much work it has accomplished,
 * and the transaction that has accomplished more has priority.
 *
 * @author Peter Veentjer.
 */
public class KarmaContentionManager implements ContentionManager {

    @Override
    public void resolve(Transaction me, Transaction other) {
        throw new TodoException();
    }
}
