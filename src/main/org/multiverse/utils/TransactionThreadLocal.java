package org.multiverse.utils;

import org.multiverse.api.Transaction;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A {@link ThreadLocal} that contains the current {@link Transaction}.
 *
 * @author Peter Veentjer.
 */
public final class TransactionThreadLocal {

    public final static AtomicLong getCount = new AtomicLong();
    public final static AtomicLong setCount = new AtomicLong();

    public final static boolean STATISTICS_ENABLED = false;

    public final static ThreadLocal<Transaction> local = new ThreadLocal<Transaction>();

    public static Transaction getThreadLocalTransaction() {
        if (STATISTICS_ENABLED) {
            getCount.incrementAndGet();
        }

        return local.get();
    }

    public static void setThreadLocalTransaction(Transaction newTransaction) {
        if (STATISTICS_ENABLED) {
            setCount.incrementAndGet();
        }

        local.set(newTransaction);
    }

    //we don't want any instances.
    private TransactionThreadLocal() {
    }
}
