package org.codehaus.multiverse.multiversionedstm;

/**
 * A Holder for an StmObject reference. If a StmObject references to another StmObject, it is best
 * to use a holder. The advantage of a holder is that the same holder can be shared between multiple
 * references, so the actual instance needs to be retrieved only once.
 *
 * @author Peter Veentjer
 * @param <S>
 */
public interface UnloadedHolder<S extends StmObject> {

    S getAndLoadIfNeeded();

    long getHandle();
}
