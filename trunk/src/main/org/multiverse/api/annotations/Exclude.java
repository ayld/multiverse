package org.multiverse.api.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * Can be placed on a field of an AtomicObject to exclude it. So this field is for the
 * stm completely invisible; as if it doesn't exist.
 *
 * Functionality is not supported yet.
 *
 * @author Peter Veentjer
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Exclude {
}
