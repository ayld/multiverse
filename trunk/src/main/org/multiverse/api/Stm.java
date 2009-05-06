package org.multiverse.api;

/**
 * The Software Transactional Memory interface.
 * <p/>
 * All operations are done through a {@link Transaction}.
 *
 * @author Peter Veentjer.
 */
public interface Stm {

    /**
     * Starts a Transaction.
     * <p/>
     * Method is threadsafe.
     *
     * @return the Transaction (that is active).
     */
    Transaction startTransaction();
}
