package org.multiverse.api;

/**
 * A {@link Tranlocal} can have different 'dirtiness' states.
 *
 * @author Peter Veentjer.
 */
public enum DirtinessStatus {
    fresh, dirty, clean, committed, conflict
}
