package org.multiverse.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation can be placed on fields of objects that have the {@link TmEntity} annotation,
 * to indicate that this field needs to be completely ignored by the {@link Stm}. It could be
 * compared to the transient fields an object serialization.
 *
 * @author Peter Veentjer.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Ignore {
}
