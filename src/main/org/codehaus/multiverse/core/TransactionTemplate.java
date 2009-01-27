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
 *          Stack<E> stack = (Stack<E>)t.read(stackHandle);
 *          return stack.take();
 *     }
 * }.execute();
 * </pre>
 * <p/>
 * It really is a shame that closures are not going to be part of Java 7, so we still need to use this
 * ugly anonymous innerclass syntax.
 * <p/>
 * The Transaction used in this TransactionTemplate also can be accessed under the TransactionThreadLocal, so
 * you don't need to drag the instanceof of the Transaction around.
 *
 * @author Peter Veentjer.
 * @param <E> the type of the object to return.
 */
public abstract class TransactionTemplate<E> {

    private final Stm stm;
    private int maximumRetryCount = 0;

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
     * Gets the maximum retry count. 0 indicates never stop retrying.
     *
     * @return the maximum retry count.
     */
    public int getMaximumRetryCount() {
        return maximumRetryCount;
    }

    /**
     * Sets the maximum retry count. A value of 0 means never stop retrying.
     *
     * @param maximumRetryCount the value to set.
     * @throws IllegalArgumentException if maximumRetryCount smaller than 0.
     */
    public void setMaximumRetryCount(int maximumRetryCount) {
        if (maximumRetryCount < 0) throw new IllegalArgumentException();
        this.maximumRetryCount = maximumRetryCount;
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
            Transaction predecessor = null;
            int retryCount = 0;
            do {
                Transaction transaction = startTransaction(predecessor);
                predecessor = null;
                boolean succes = false;
                TransactionThreadLocal.set(transaction);
                try {
                    E result = execute(transaction);
                    transaction.commit();
                    succes = true;
                    return result;
                } catch (RetryError ex) {
                    retryCount++;
                    predecessor = transaction;
                } catch (WriteConflictException ex) {
                    retryCount++;
                    predecessor = transaction;
                } finally {
                    TransactionThreadLocal.remove();

                    if (succes)
                        transaction.commit();
                    else
                        transaction.abort();
                }
            } while (retryCount < maximumRetryCount || maximumRetryCount == 0);

            throw new TooManyRetriesException();
        } catch (InterruptedException ex) {
            Thread.interrupted();
            throw new RuntimeException(ex);
        } catch (Exception ex) {
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
