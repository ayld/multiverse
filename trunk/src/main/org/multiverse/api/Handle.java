package org.multiverse.api;

/**
 * Uniquely identifies an object attached to a Transaction. Can be used to retrieve instances from the STM
 * after they are committed.
 *
 * @author Peter Veentjer.
 * @param <T> the type of the object this Handle points to.
 */
public interface Handle<T> {
}
