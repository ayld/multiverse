package org.multiverse.contentionmanagers;

import org.multiverse.api.Transaction;
import org.multiverse.utils.TodoException;

/**
 * Priority: Each transaction takes a timestamp when it starts. If A has an older
 * timestamp than B, it aborts B, and otherwise it waits. A transaction that
 * restarts after an abort keeps its old timestamp, ensuring that every transaction
 * eventually completes.
 *
 * @author Peter Veentjer.
 */
public class PriorityContentionManager implements ContentionManager {

    @Override
    public void resolve(Transaction me, Transaction other) {
        throw new TodoException();
    }
}
