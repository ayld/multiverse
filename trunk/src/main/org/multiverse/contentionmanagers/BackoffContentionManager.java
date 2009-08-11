package org.multiverse.contentionmanagers;

import org.multiverse.api.Transaction;
import org.multiverse.utils.TodoException;

/**
 * Backoff: A repeatedly backs off for a random duration, doubling the expected
 * time up to some limit. When that limit is reached, it aborts B.
 *
 * @author Peter Veentjer.
 */
public class BackoffContentionManager implements ContentionManager {

    @Override
    public void resolve(Transaction me, Transaction other) {
        throw new TodoException();
    }
}
