package org.codehaus.multiverse.api;

/**
 * An id that uniquely identifies a transaction.
 * <p/>
 * A TransactionId is immutable and threadsafe.
 *
 * @author Peter Veentjer.
 */
public class TransactionId {

    private final String text;

    public TransactionId(String text) {
        assert text != null;
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }

    @Override
    public int hashCode() {
        return text.hashCode();
    }

    @Override
    public boolean equals(Object thatObj) {
        if (this == thatObj)
            return true;

        if (!(thatObj instanceof TransactionId))
            return false;

        TransactionId that = (TransactionId) thatObj;
        return that.text.equals(this.text);
    }
}
