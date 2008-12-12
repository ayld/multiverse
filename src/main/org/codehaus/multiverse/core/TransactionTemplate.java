package org.codehaus.multiverse.core;

import org.codehaus.multiverse.core.RetryException;
import org.codehaus.multiverse.core.Transaction;
import org.codehaus.multiverse.core.WriteConflictException;
import org.codehaus.multiverse.core.Stm;

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
                Transaction transaction = predecessor == null ? stm.startTransaction() : stm.startRetriedTransaction(predecessor);
                try {
                    predecessor = null;
                    result = execute(transaction);
                    transaction.commit();
                    success = true;
                } catch (RetryException ex) {
                    //System.out.println(Thread.currentThread() + " retried");
                    transaction.abort();
                    predecessor = transaction;
                } catch (WriteConflictException ex) {
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
