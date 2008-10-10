package org.codehaus.multiverse.multiversionedstm;

public enum ValidateStatus {

    noReadsOrWrites, onlyReads, hasNonconflictingWrites,  hasConflictingWrites
}
