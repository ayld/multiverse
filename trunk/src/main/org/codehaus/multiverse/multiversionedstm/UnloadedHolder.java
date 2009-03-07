package org.codehaus.multiverse.multiversionedstm;

public interface UnloadedHolder<S extends StmObject> {

    S getAndLoadIfNeeded(MyTransaction transaction);

    long getHandle();
}
