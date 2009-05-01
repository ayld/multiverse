package org.multiverse.multiversionedstm;

/**
 * A callback interface that can be used to walk over members of a MaterializedObject.
 *
 * @author Peter Veentjer.
 */
public interface MemberWalker {

    void onMember(MaterializedObject member);
}
