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
     * Returns the Transaction this StmObject is part of. The transaction is set on a mutable StmObject when it is
     * attached to a transaction or loaded through a transaction. If the StmObject is new or immutable null is returned
     * since it doesn't need to use a transaction, so that it can be shared between transactions (optimization).
     * <p/>
     * todo: what about immutable objects.
     *
     * @return the Transaction this StmObject is part of, or null if it isn't attached to a transaction or immutable.
     */
    MyTransaction ___getTransaction();

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
     * A fresh StmObject always is dirty by default.
     * <p/>
     * todo: what about immutable objects.
     *
     * @return true if this StmObject is dirty (so needs to be written to heap), false otherwise.
     */
    boolean ___isDirtyIgnoringStmMembers();

    /**
     * Checks if this StmObject and all objects that can be reached from it are immutable. Immutable means
     * that the pre-instrumented StmObject doesn't have any modifyable state, so if there is any field (primitive
     * non primitive) or stmobject reachable from that object, can be updated, it is mutable.
     * <p/>
     * for example:
     * A Queue uses 2 stacks, and since the stacks are not immutable, the queue isn't immutable even though
     * itself doesn't contain any state (state is in the stacks).
     * <p/>
     * another example:
     * the IntegerConstant is immutable, and doesn't have any dependencies, so it is an immutable object
     * graph.
     *
     * @return true if this object including all dependencies are immutable.
     */
    boolean ___isImmutableObjectGraph();

    /**
     * Attaches the StmObject to a transaction. No checks are done if the object already is connected to another
     * transaction. It is up the the stm implementation to deal with this.
     * <p/>
     * todo: what about immutable objects.
     *
     * @param transaction the transaction this StmObject attaches to. The value should not be null.
     */
    void ___onAttach(MyTransaction transaction);
}
