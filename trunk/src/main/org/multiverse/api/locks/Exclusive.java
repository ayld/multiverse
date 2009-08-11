package org.multiverse.api.locks;

import java.lang.annotation.*;

/**
 * Can be placed on a object reference in an atomicobject to load the object one needed
 * in exclusive mode.
 *
 * @author Peter Veentjer
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Exclusive {
}
