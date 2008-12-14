package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.core.RetryError;

/**
 * @author Peter Veentjer.
 */
public final class TransactionMethods {

    /**
     * Retries the current transaction. This method should be called when no progress can be made. It can
     * be compared to the STM implementation of the condition variable.
     */
    public static void retry() {
        throw RetryError.INSTANCE;
    }

    //we don't want instances
    private TransactionMethods() {
    }
}
