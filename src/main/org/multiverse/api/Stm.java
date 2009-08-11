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
     * Starts a Transaction that can be used for writes.
     *
     * @return the created Transaction.
     */
    Transaction startUpdateTransaction();

    /**
     * Starts a readonly Transaction.
     *
     * @return the created readonly Transaction.
     */
    Transaction startReadOnlyTransaction();

    /**
     * Starts a readonly Transaction with the provided readVersion. Normally a readonly
     * transaction starts with the clockVersion of the stm. But if a value is provided
     * from the outside, you are able to look back in time. In Oracle this is called a
     * flashback query.
     * <p/>
     * It depends on the Stm how far a transaction is able to look back in time.
     *
     * @param readVersion the readversion.
     * @return the created readonly Transaction.
     * @throws IllegalArgumentException if the readVersion is larger than the clockVersion.
     */
    Transaction startFlashbackTransaction(long readVersion);
}
