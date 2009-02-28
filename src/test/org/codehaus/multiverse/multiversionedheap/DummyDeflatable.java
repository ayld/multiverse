package org.codehaus.multiverse.multiversionedheap;

import org.codehaus.multiverse.multiversionedstm.HandleGenerator;

public class DummyDeflatable implements Deflatable {

    private final long handle;
    private String value;

    public DummyDeflatable(long handle) {
        this(handle, "dummy");
    }

    public DummyDeflatable(String value) {
        this(HandleGenerator.createHandle(), value);
    }

    public DummyDeflatable(long handle, String value) {
        this.handle = handle;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public long ___getHandle() {
        return handle;
    }

    public Deflated ___deflate(long version) {
        return new DummyDeflated(this, version);
    }
}
