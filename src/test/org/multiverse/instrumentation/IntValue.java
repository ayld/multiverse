package org.multiverse.instrumentation;

import org.multiverse.api.annotations.TmEntity;

@TmEntity
public final class IntValue {

    private int value;

    public IntValue(){
        this.value = 0;
    }

    public IntValue(int value) {
        this.value = value;
    }

    public void inc() {
        value++;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }

    @Override
    public int hashCode() {
        return value;
    }

    @Override
    public boolean equals(Object thatObj) {
        if (this == thatObj) {
            return true;
        }

        if (!(thatObj instanceof IntValue)) {
            return false;
        }

        IntValue that = (IntValue) thatObj;
        return that.value == this.value;
    }
}
