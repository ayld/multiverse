package org.multiverse.api;

/**
 * A ThreadLocal that contains the current {@link Transaction}.
 *
 * @author Peter Veentjer
 */
public final class TransactionThreadLocal {

    private static final ThreadLocal<Transaction> transactionThreadLocal = new ThreadLocal<Transaction>();

    /**
     * Gets the current transaction. If no transaction is set, null is returned.
     *
     * @return the current transaction, or null of none is set.
     */
    public static Transaction get() {
        return transactionThreadLocal.get();
    }

    /**
     * Removes the current transaction reference in this TransactionThreadLocal. If no transaction is
     * set, the call is ignored.
     * <p/>
     * This call is made package private so that only the stm implementation is able to call it.
     */
    static void remove() {
        transactionThreadLocal.set(null);
    }

    /**
     * Sets the transaction on this TransactionThreadLocal.
     * <p/>
     * This call is made package private so that only the stm implementation is able to call it.
     *
     * @param transaction the Transaction  to set.
     * @throws NullPointerException  if transaction is null.
     * @throws IllegalStateException if there already is a transaction set. Nested transactions are not
     *                               supported yet.
     */
    static void set(Transaction transaction) {
        if (transaction == null)
            throw new NullPointerException();

        if (get() != null) {
            throw new IllegalStateException("Another transaction already is set on the threadlocal, " +
                    "nested transactions are not allowed (yet).");
        }

        transactionThreadLocal.set(transaction);
    }
}
