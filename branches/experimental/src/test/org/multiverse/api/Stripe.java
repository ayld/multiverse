package org.multiverse.api;

import org.multiverse.datastructures.refs.ManagedRef;

/**
 * The goal of the stripe is to assist with helping to find a non conflicting
 * execution of a transaction.
 *
 * @author Peter Veentjer
 */
public interface Stripe<E> {

    ManagedRef<E> getFreeRef();

    ManagedRef<E> getOccupiedRef();

    boolean isFull();
}
