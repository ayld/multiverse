package org.multiverse.multiversionedstm;

import org.multiverse.api.Originator;

/**
 * Represents an Object that can be stores inside the {@link MultiversionedStm}.
 *
 * @author Peter Veentjer.
 */
public interface MaterializedObject {

    Originator getOriginator();

    boolean isDirty();

    DematerializedObject dematerialize();

    void memberTrace(MemberTracer memberTracer);


    MaterializedObject getNextInChain();

    void setNextInChain(MaterializedObject next);
}
