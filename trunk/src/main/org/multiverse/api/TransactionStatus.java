package org.multiverse.api;

/**
 * An enumeration containing the different states a {@link Transaction} can be in. Every
 * transaction begins with the active status. If the transaction is able to commit, the status
 * will change to committed. Or when it is is aborted, the status will change to aborted.
 *
 * @see Transaction#getStatus()
 * @author Peter Veentjer.
 */
public enum TransactionStatus {

    active, committed, aborted
}
