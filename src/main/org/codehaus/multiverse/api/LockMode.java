package org.codehaus.multiverse.api;

public enum LockMode {
    free(0), shared(1), exclusive(2);

    private final int lockLevel;

    LockMode(int lockLevel) {
        this.lockLevel = lockLevel;
    }

    public boolean isStrongerThan(LockMode that) {
        return lockLevel > that.lockLevel;
    }
}
