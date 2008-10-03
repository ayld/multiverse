package org.codehaus.stm.multiversionedstm;

public enum ValidateStatus {

    noReadsOrWrites, onlyReads, hasNonconflictingWrites,  hasConflictingWrites
}
