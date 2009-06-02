package org.multiverse.api.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that can be placed on a field to indicate that this field should not be directly
 * managed by the transaction. This is done to reduce stress on the transaction for inner objects.
 * For more information see {@link org.multiverse.api.Transaction#readSelfManaged(org.multiverse.api.Handle)}.
 *
 * @author Peter Veentjer.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface NonEscaping {
}