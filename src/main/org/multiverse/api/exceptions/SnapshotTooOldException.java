package org.multiverse.api.exceptions;

public class SnapshotTooOldException extends StmException {

    public SnapshotTooOldException() {
    }

    public SnapshotTooOldException(String msg) {
        super(msg);
    }
}