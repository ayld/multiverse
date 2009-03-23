package org.codehaus.multiverse.utils;

import static java.lang.String.format;

public final class Pair<A, B> {
    private final A a;
    private final B b;

    public Pair(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public A getA() {
        return a;
    }

    public B getB() {
        return b;
    }

    @Override
    public String toString() {
        return format("Pair(%s,%s)", a, b);
    }

    @Override
    public int hashCode() {
        return hashCode(a) + hashCode(b);
    }

    private int hashCode(Object o) {
        return o == null ? 0 : o.hashCode();
    }

    @Override
    public boolean equals(Object thatObj) {
        if (thatObj == this)
            return true;

        if (!(thatObj instanceof Pair))
            return false;

        Pair that = (Pair) thatObj;
        if (!equals(that.a, this.a))
            return false;

        if (!equals(that.b, this.b))
            return false;

        return true;
    }

    private boolean equals(Object o1, Object o2) {
        if (o1 == null)
            return o2 == null;

        return o1.equals(o2);
    }
}
