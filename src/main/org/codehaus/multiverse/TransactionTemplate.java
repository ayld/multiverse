package org.codehaus.multiverse;

import org.codehaus.multiverse.transaction.AbortedException;
import org.codehaus.multiverse.transaction.RetryException;
import org.codehaus.multiverse.transaction.Transaction;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * The TransactionTemplate  is a template that contains all plumbing logic for the start, retry etc of
 * Transactions. It could be compared to one of the Spring templates like the JdbcTemplate.
 *
 * @author Peter Veentjer.
 */
public abstract class TransactionTemplate<E> {

    private final Stm stm;

    protected TransactionTemplate(Stm stm) {
        if (stm == null) throw new NullPointerException();
        this.stm = stm;
    }

    public Stm getStm() {
        return stm;
    }

    abstract protected E execute(Transaction t) throws Exception;

    public final E execute() {
        try {
            boolean success = false;
            Transaction predecessor = null;
            E result = null;
            do {
                Transaction transaction = predecessor == null ? stm.startTransaction() : stm.startTransaction(predecessor);
                try {
                    predecessor = null;
                    result = execute(transaction);
                    transaction.commit();
                    success = true;
                } catch (RetryException ex) {
                    //System.out.println(Thread.currentThread() + " retried");
                    transaction.abort();
                    predecessor = transaction;
                } catch (AbortedException ex) {
                    //System.out.println(Thread.currentThread() + " aborted");
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
