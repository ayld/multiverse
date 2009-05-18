package org.multiverse.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An Eager annotation can be placed on fields of TmEnties to indicate that the field needs to be
 * loaded eagerly. The default is lazy loading.
 *
 * @author Peter Veentjer.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Eager {
}
