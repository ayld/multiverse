package org.multiverse.instrumentation.integration;

import org.multiverse.api.annotations.TmEntity;

@TmEntity
public class Outer {

    private Inner inner;
    private int value;

    public Outer(int outerValue, int innerValue) {
        inner = new Inner();
        inner.value = innerValue;
        value = outerValue;
    }

    public Inner getInner() {
        return inner;
    }

    public int getValue() {
        return value;
    }

    @TmEntity
    public class Inner {
        private int value;

        public void inc() {
            value++;
        }

        public int getValue() {
            return value;
        }

        public Outer getOuter() {
            return Outer.this;
        }
    }
}
