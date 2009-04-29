package org.multiverse.util;

public final class RetryCounter {

    private int value;

    public RetryCounter(int value) {
        this.value = value;
    }

    public boolean decrease() {
        if (value <= 0)
            return false;

        value--;
        return true;
    }
}
