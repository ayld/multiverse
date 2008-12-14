package org.codehaus.multiverse.core;

/**
 * The TransactionTemplate  is a template that contains all plumbing logic for the start, retry etc of
 * Transactions. It could be compared to one of the Spring templates like the JdbcTemplate.
 * <p/>
 * todo: protection against livelock
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
                Transaction transaction = createTransaction(predecessor);

                try {
                    predecessor = null;
                    result = execute(transaction);
                    transaction.commit();
                    success = true;
                } catch (RetryError ex) {
                    //System.out.println(Thread.currentThread() + " retried");
                    transaction.abort();
                    predecessor = transaction;
                } catch (WriteConflictException ex) {

                } catch (RuntimeException ex) {
                    transaction.abort();
                    throw ex;
                } catch (Exception ex) {
                    transaction.abort();
                    throw new RuntimeException(ex);
                }
            } while (!success);

            return result;
        } catch (InterruptedException ex) {
            Thread.interrupted();
            throw new RuntimeException(ex);
        }
    }

    private Transaction createTransaction(Transaction predecessor) throws InterruptedException {
        if (predecessor == null)
            return stm.startTransaction();
        else
            return stm.startRetriedTransaction(predecessor);
    }
}
