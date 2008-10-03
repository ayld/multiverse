package org.codehaus.stm;

import org.codehaus.stm.transaction.AbortedException;
import org.codehaus.stm.transaction.RetryException;
import org.codehaus.stm.transaction.Transaction;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Idea:
 */
public abstract class TransactionTemplate {

    private final Stm stm;
    private final AtomicInteger retryCount = new AtomicInteger();

    protected TransactionTemplate(Stm stm) {
        if (stm == null) throw new NullPointerException();
        this.stm = stm;
    }

    public long getRetryCount() {
        return retryCount.longValue();
    }

    public Stm getStm() {
        return stm;
    }

    abstract protected Object execute(Transaction t) throws Exception;

    public final Object execute() {
        try {
            boolean success = false;
            long[] addresses = null;
            long version = -1;
            Object result = null;
            do {
                Transaction transaction = addresses == null ? stm.startTransaction() : stm.startTransaction(addresses, version);
                try {
                    version = -1;
                    addresses = null;
                    result = execute(transaction);
                    transaction.commit();
                    success = true;
                } catch (RetryException ex) {
                    System.out.println(Thread.currentThread()+" retried");
                    transaction.abort();
                    addresses = transaction.getReadAddresses();
                    version = transaction.getVersion();
                } catch (AbortedException ex) {
                    System.out.println(Thread.currentThread()+" aborted");
                    transaction.abort();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            } while (!success);

            return result;
        } catch (InterruptedException ex) {
            Thread.interrupted();
            throw new RuntimeException(ex);
        }
    }
}
