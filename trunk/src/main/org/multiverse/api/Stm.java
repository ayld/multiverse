package org.multiverse.api;

/**
 * The main interface for software transactional memory. Updates/reads in the stm should
 * only be done through the {@link Transaction} interface. So see that for more details.
 *
 * @author Peter Veentjer.
 */
public interface Stm {

    /**
     * Returns the current clock version.
     *
     * @return the current clock version.
     */
    long getClockVersion();

    /**
     * Starts a Transaction that can be used for updates.
     *
     * @param familyName the familyName of the Transaction.
     * @return the created Transaction.
     */
    Transaction startUpdateTransaction(String familyName);

    /**
     * Starts Transaction that only can be used for readonly access.
     *
     * @param familyName the familyName of the Transaction.
     * @return the created readonly Transaction.
     */
    Transaction startReadOnlyTransaction(String familyName);
}
