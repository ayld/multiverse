package org.multiverse.api;

/**
 * The Software Transactional Memory interface.
 * <p/>
 * All operations are done through a {@link Transaction}.
 * <p/>
 * All Stm's are threadsafe to use (of course).
 *
 * @author Peter Veentjer.
 */
public interface Stm {

    /**
     * Starts a Transaction.
     *
     * @return the started Transaction.
     */
    Transaction startTransaction();
}
