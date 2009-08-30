package org.multiverse.stms.alpha.retry;

import org.multiverse.api.exceptions.RetryError;
import org.multiverse.stms.alpha.UpdateAlphaTransaction;

public class ImmediatelyRetryPolicy implements RetryPolicy {

    @Override
    public void retry(UpdateAlphaTransaction t) {
        throw RetryError.create();
    }
}
