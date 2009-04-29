package org.multiverse.multiversionedstm;

import org.multiverse.api.Originator;

public interface MaterializedObject {

    Originator getOriginator();

    boolean isDirty();

    DematerializedObject dematerialize();

    void memberTrace(MemberTracer memberTracer);


    MaterializedObject getNextInChain();
    
    void setNextInChain(MaterializedObject next);
}
