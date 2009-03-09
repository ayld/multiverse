package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.multiversionedheap.Deflatable;

import java.util.Iterator;

/**
 * Each persistant object that wants to live in the {@link MultiversionedStm} needs to implement this interface.
 * In the end some kind of bytecode instrumentation can add this interface and implement the methods automatically
 * on certain pojo's.
 * <p/>
 * The reason that all methods have such a strange prefix, is that normal pojo's will be instrumented so that they
 * implement this interface. The prefix prevents name clashes.
 * <p/>
 * todo:
 * what happens to an StmObject once the transaction has finished (commit or abort). Is it still attached
 * to a dead transaction.
 * <p/>
 * Dehydration and hydration of immutable stmObjects:
 * Since immutable StmObjects can't change, there is no reason to return different instances. The same instance
 * can be shared between transaction and this makes working with immutable StmObjects very cheap.
 *
 * @author Peter Veentjer.
 */
public interface StmObject extends Deflatable {

    /**
     * Returns the handle of this StmObject in the heap. A non 0 value always will be returned, no matter
     * if the object has never been persisted before.
     *
     * @return the handle of this StmObject in the heap.
     */
    long ___getHandle();

    /**
     * Returns an Iterator over all fresh or loaded StmObject members. Other types of members, like primitives
     * and other non StmObjects, or ignored.
     * <p/>
     * The returned iterator is allowed to contain cycles. It is up to the caller to deal with it.
     *
     * @return the iterator over all loaded or fresh member StmObjects.
     */
    Iterator<StmObject> ___getFreshOrLoadedStmMembers();

    /**
     * Checks if this StmObject is dirty. This is needed to figure out if changes made in this stm object
     * need to be written to heap. State changes in stmmembers, are ignored.
     * <p/>
     * A fresh StmObject always is dirty by default. After the object is dehydrated it is not dirty anymore
     * unless new changes are made.
     *
     * @return true if this StmObject is dirty (so needs to be written to heap), false otherwise.
     */
    boolean ___isDirtyIgnoringStmMembers();

}
