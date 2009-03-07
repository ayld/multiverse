package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.core.Transaction;

public interface MyTransaction extends Transaction {

    /**
     * @param handle
     * @param <S>
     * @return the found Holder, or null if no holder is found
     */
    <S extends StmObject> UnloadedHolder<S> readHolder(long handle);
}
