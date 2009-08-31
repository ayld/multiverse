package org.multiverse;

import static java.lang.Boolean.getBoolean;

/**
 * The program should behave exactly the same with all the debug switches disabled. But some costly
 * checks can be activated this way to see if no programming errors were made.
 *
 * @author Peter Veentjer
 */
public interface MultiverseConstants {

    public static final boolean SANITY_CHECK_ENABLED = getBoolean(System.getProperty("multiverse.sanitychecksenabled", "true"));
}
