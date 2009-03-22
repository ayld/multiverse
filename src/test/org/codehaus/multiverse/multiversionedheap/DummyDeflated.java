package org.codehaus.multiverse.multiversionedheap;

import org.codehaus.multiverse.api.Transaction;

import static java.lang.String.format;

public class DummyDeflated extends AbstractDeflated {

    private String value;

    public DummyDeflated(long handle) {
        this(handle, 1, "dummy");
    }

    public DummyDeflated(long handle, long version, String value) {
        super(handle, version);
        this.value = value;
    }

    public DummyDeflated(StringDeflatable deflatable, long version) {
        super(deflatable.___getHandle(), version);
        this.value = deflatable.getValue();
    }

    public String getValue() {
        return value;
    }

    public StringDeflatable ___inflate(Transaction transaction) {
        throw new RuntimeException();
    }

    public String toString() {
        return format("DummyDeflated(handle=%s, version=%s, get=%s)", ___getHandle(), ___getVersion(), getValue());
    }

    @Override
    public boolean equals(Object thatObj) {
        if (this == thatObj)
            return true;

        if (!(thatObj instanceof DummyDeflated))
            return false;

        DummyDeflated that = (DummyDeflated) thatObj;

        if (that.___getHandle() != this.___getHandle())
            return false;

        if (that.___getVersion() != this.___getVersion())
            return false;

        if (that.getValue() == null) {
            if (this.getValue() != null)
                return false;
        } else if (!that.getValue().equals(this.getValue()))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return new Long(___getHandle()).hashCode();
    }
}
