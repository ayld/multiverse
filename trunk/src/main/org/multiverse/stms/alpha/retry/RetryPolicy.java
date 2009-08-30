package org.multiverse.stms.alpha.retry;

import org.multiverse.stms.alpha.UpdateAlphaTransaction;

public interface RetryPolicy {

    void retry(UpdateAlphaTransaction t);
}
