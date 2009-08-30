package org.multiverse.contentionmanagers;

import org.multiverse.api.Transaction;
import org.multiverse.utils.TodoException;

/**
 * Greedy: Each transaction takes a timestamp when it starts. A aborts B if either
 * A has an older timestamp than B, or B is waiting for another transaction. This
 * strategy eliminates chains of waiting transactions. As in the priority policy,
 * every transaction eventually completes.
 *
 * @author Peter Veentjer.
 */
public class GreedyContentionManager implements ContentionManager {
    @Override
    public void resolve(Transaction me, Transaction other) {
        throw new TodoException();
    }
}
