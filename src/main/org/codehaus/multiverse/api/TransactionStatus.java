package org.codehaus.multiverse.api;

/**
 * An enumaration of all states a {@link Transaction} can be in.
 *
 * @author Peter Veentjer
 */
public enum TransactionStatus {
    active, aborted, committed
}
