package org.multiverse.stms.alpha.retry;

import org.multiverse.stms.alpha.UpdateTransaction;

public interface RetryPolicy {

    void retry(UpdateTransaction t);
}
