package org.multiverse.api.exceptions;

/**
 * A {@link StmException} that indicates that the snapshot containing historical data doesn't not
 * exist anymore.
 *
 * @author Peter Veentjer.
 */
public class SnapshotTooOldException extends StmException {

    public SnapshotTooOldException() {
    }

    public SnapshotTooOldException(String msg) {
        super(msg);
    }
}