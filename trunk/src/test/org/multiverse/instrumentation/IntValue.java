package org.multiverse.instrumentation;

import org.multiverse.api.annotations.TmEntity;

@TmEntity
public class IntValue {
    private int value;

    public IntValue(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
