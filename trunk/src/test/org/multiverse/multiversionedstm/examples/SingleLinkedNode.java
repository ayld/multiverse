package org.multiverse.multiversionedstm.examples;

import org.multiverse.multiversionedstm.DematerializedObject;
import org.multiverse.multiversionedstm.MaterializedObject;
import org.multiverse.multiversionedstm.MemberWalker;
import org.multiverse.multiversionedstm.MultiversionedHandle;

public class SingleLinkedNode implements MaterializedObject {


    @Override
    public MultiversionedHandle getHandle() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isDirty() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public DematerializedObject dematerialize() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void walkMaterializedMembers(MemberWalker memberWalker) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public MaterializedObject getNextInChain() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setNextInChain(MaterializedObject next) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
