package org.multiverse.api.locks;

import java.lang.annotation.*;

/**
 * Can be placed on a reference of an AtomicObject to load the reference one loaded
 * with a shared lock.
 *
 * Functionality is not supported yet.. so don't use it
 *
 * @author Peter Veentjer
 * @see org.multiverse.api.locks.Exclusive
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Shared {
}
