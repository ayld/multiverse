package org.multiverse.stms.alpha;

/**
 * A {@link org.multiverse.stms.alpha.Tranlocal} can have different 'dirtiness' states.
 *
 * @author Peter Veentjer.
 */
public enum DirtinessStatus {
    fresh, dirty, clean, committed, conflict
}
