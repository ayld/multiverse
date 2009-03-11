package org.codehaus.multiverse.api;

/**
 * An id that uniquely identifies a transaction.
 *
 * @author Peter Veentjer.
 */
public class TransactionId {

    private final String name;

    public TransactionId(String name) {
        assert name != null;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object thatObj) {
        if (this == thatObj)
            return true;

        if (!(thatObj instanceof TransactionId))
            return false;

        TransactionId that = (TransactionId) thatObj;
        return that.name.equals(this.name);
    }
}
