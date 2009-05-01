package org.multiverse.multiversionedstm;

/**
 * Represents an Object that can be stores inside the {@link MultiversionedStm}.
 *
 * @author Peter Veentjer.
 */
public interface MaterializedObject {

    MultiversionedHandle getHandle();


    /**
     * Checks if this MaterializedObject object is dirty (so needs to be persisted).
     *
     * @return
     */
    boolean isDirty();

    DematerializedObject dematerialize();


    /**
     * @param memberWalker
     */
    void walkMaterializedMembers(MemberWalker memberWalker);


    MaterializedObject getNextInChain();

    void setNextInChain(MaterializedObject next);
}
