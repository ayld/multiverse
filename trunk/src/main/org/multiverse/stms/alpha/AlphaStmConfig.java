package org.multiverse.stms.alpha;

import org.multiverse.utils.commitlock.CommitLockPolicy;
import org.multiverse.utils.commitlock.GenericCommitLockPolicy;

/**
 * An Object responsible for storing the configuration for an {@link AlphaStm}. So instead
 * of having to deal with ever growing number of arguments, the AlphaStm constructor only
 * needs a single argument.
 * <p/>
 * Once the AlphaStm is constructed, changes made to the config object is not visible to
 * the stm that received. It wil have copied all fields, and doesn't read from the config
 * after creation anymore.
 * <p/>
 * AlphaStm are not threadsafe. They can be shared between threads as long as no changes
 * are to the config and there is a save handover point from construction to usage. A volatile
 * variable or mutex would do the trick. As long as there is a happens before relation
 * between the write and the read expressed in terms of the JMM.
 * <p/>
 * A config will always be checked by the constructor of the stm if all fields are correctly
 * initialized.
 *
 * @author Peter Veentjer.
 */
public final class AlphaStmConfig {

    public AlphaStmStatistics statistics = new AlphaStmStatistics();

    public boolean loggingPossible = true;

    public CommitLockPolicy commitLockPolicy = GenericCommitLockPolicy.FAIL_FAST_BUT_RETRY;

    public void ensureValid() {
        if (commitLockPolicy == null) {
            throw new RuntimeException("commitLockPolicy can't be null");
        }
    }

    public static AlphaStmConfig createDebugConfig() {
        AlphaStmConfig config = new AlphaStmConfig();
        config.loggingPossible = true;
        return config;
    }

    public static AlphaStmConfig createFastConfig() {
        AlphaStmConfig config = new AlphaStmConfig();
        config.statistics = null;
        config.loggingPossible = false;
        return config;
    }
}
