package org.multiverse.api.annotations;

import java.lang.annotation.*;

/**
 * Can be placed on an object to make it Atomic. See the {@link AtomicMethod} for more information.
 *
 * @author Peter Veentjer.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface AtomicObject {

}
