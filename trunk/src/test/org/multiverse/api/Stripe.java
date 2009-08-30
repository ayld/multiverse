package org.multiverse.api;

/**
 * The goal of the stripe is to assist with helping to find a non conflicting
 * execution of a transaction.
 *
 * @author Peter Veentjer
 */
public interface Stripe<E> {

    PredictingManagedRef<E> getFreeRef();

    PredictingManagedRef<E> getOccupiedRef();

    boolean isFull();
}
