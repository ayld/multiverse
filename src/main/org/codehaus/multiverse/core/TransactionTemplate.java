package org.codehaus.multiverse.core;

/**
 * The TransactionTemplate  is a template that contains all plumbing logic for the start, retry etc of
 * Transactions. TransactionTemplate is the prefered way to deal with a {@link Transaction} and {@link Stm}.
 * <p/>
 * It could be compared to one of the Spring templates like the JdbcTemplate.
 * <p/>
 * example:
 * <pre>
 * String result = new TransactionTemplate(stm){
 *     public E execute(Transaction t){
 *          Stack stack = (Stack)t.read(stackHandle);
 *          return stack.take();
 *     }
 * }.execute();
 * </pre>
 * <p/>
 * It really is a shame that closures are not going to be part of Java 7, so we still need to use this
 * ugly anonymous innerclass syntax.
 *
 * @author Peter Veentjer.
 * @param <E> the type of the object to return.
 */
public abstract class TransactionTemplate<E> {

    private final Stm stm;

    /**
     * Creates a new TransactionTemplate.
     *
     * @param stm the Stm this TransactionTemplate uses.
     * @throws NullPointerException if stm is null.
     */
    protected TransactionTemplate(Stm stm) {
        if (stm == null) throw new NullPointerException();
        this.stm = stm;
    }

    /**
     * Returns the Stm used by this TransactionTemplate.
     *
     * @return the Stm used by this TransactionTemplate.
     */
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
                Transaction transaction = startTransaction(predecessor);
                try {
                    predecessor = null;
                    result = execute(transaction);
                    transaction.commit();
                    success = true;
                } catch (RetryError ex) {
                    transaction.abort();
                    predecessor = transaction;
                } catch (WriteConflictException ex) {
                    //todo: a write conflict should not be retried?
                    transaction.abort();
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

    private Transaction startTransaction(Transaction predecessor) throws InterruptedException {
        if (predecessor == null)
            return stm.startTransaction();
        else
            return stm.startRetriedTransaction(predecessor);
    }
}
