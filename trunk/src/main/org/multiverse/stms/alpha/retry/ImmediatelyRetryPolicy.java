package org.multiverse.stms.alpha.retry;

import org.multiverse.api.exceptions.RetryError;
import org.multiverse.stms.alpha.UpdateTransaction;

public class ImmediatelyRetryPolicy implements RetryPolicy {

    @Override
    public void retry(UpdateTransaction t) {
        throw RetryError.create();
    }
}
