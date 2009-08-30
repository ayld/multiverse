package org.multiverse.api;

import org.multiverse.datastructures.refs.ManagedRef;

public interface PredictingManagedRef<E> extends ManagedRef<E> {

    boolean invisibleIsNull();

    boolean invisibleIsNotNull();

    boolean invisibleHasWritePending();
}
