package org.multiverse.stms.alpha;

/**
 * The program should behave exactly the same with all the debug switches disabled. But some costly
 * checks can be activated this way to see if no programming errors were made.
 *
 * @author Peter Veentjer
 */
public interface AlphaStmDebugConstants {

    public static final boolean SANITY_CHECK_ENABLED = true;
    public static final boolean REUSE_WriteConflictException = true;
    public static final boolean REUSE_RetryError = true;
    public static final boolean REUSE_LoadTooOldVersionException = true;
    public static final boolean REUSE_FailedToObtainLocksException = true;
    public static final boolean REUSE_LockedException = true;
}
