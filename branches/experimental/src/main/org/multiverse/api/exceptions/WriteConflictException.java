package org.multiverse.api.exceptions;

import org.multiverse.stms.alpha.AlphaStmDebugConstants;

/**
 * A {@link CommitFailureException} that indicates that a write conflict happened while
 * doing a {@link org.multiverse.api.Transaction#commit()}.
 *
 * @author Peter Veentjer.
 */
public class WriteConflictException extends CommitFailureException {

    public final static WriteConflictException INSTANCE = new WriteConflictException();

    public static WriteConflictException create() {
        if (AlphaStmDebugConstants.REUSE_WriteConflictException) {
            return WriteConflictException.INSTANCE;
        } else {
            return new WriteConflictException();
        }
    }
}
