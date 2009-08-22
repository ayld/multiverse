package org.multiverse.stms.alpha.retry;

import org.multiverse.stms.alpha.UpdateTransaction;

/**
 * One of the problems with the {@link ImmediatelyRetryPolicy} is that the transaction
 * is retried immediately, causing the progress made by the transaction being discarded.
 * <p/>
 * This could lead to a waste of resources. The SpinningRetryPolicy spins a few times and
 * if the desired write did not happen it starts to throw the RetryError.
 * <p/>
 * The big problem is that the current transaction already has read/made changes on addresses
 * it has been waiting for. So how can these changes be undone?
 *
 * @author Peter Veentjer.
 */
public class SpinningRetryPolicy implements RetryPolicy {

    public SpinningRetryPolicy() {
    }

    @Override
    public void retry(UpdateTransaction t) {

    }
}
