package org.multiverse.api.annotations;

import java.lang.annotation.*;

/**
 * An annotation for an Object that is able to live inside Stm space.
 * <p/>
 * It could be compared with the JPA Entity annotation.
 *
 * @author Peter Veentjer.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface TmEntity {

}
