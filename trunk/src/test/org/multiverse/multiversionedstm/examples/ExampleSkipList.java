package org.multiverse.multiversionedstm.examples;

import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.DematerializedObject;
import org.multiverse.multiversionedstm.MaterializedObject;
import org.multiverse.multiversionedstm.MemberWalker;
import org.multiverse.multiversionedstm.MultiversionedHandle;

public class ExampleSkipList<E> implements MaterializedObject {

    private DematerializedSkipList<E> lastDematerialized;
    private final MultiversionedHandle<ExampleSkipList<E>> handle;

    private ExampleSkipList(DematerializedSkipList<E> dematerializedSkipList, Transaction t) {
        this.handle = dematerializedSkipList.handle;
        this.lastDematerialized = dematerializedSkipList;
    }

    @Override
    public MultiversionedHandle getHandle() {
        return handle;
    }

    @Override
    public boolean isDirty() {
        if (lastDematerialized == null)
            return true;

        //todo

        return false;
    }

    @Override
    public DematerializedObject dematerialize() {
        return lastDematerialized = new DematerializedSkipList<E>(this);
    }

    @Override
    public void walkMaterializedMembers(MemberWalker memberWalker) {
        throw new RuntimeException();
    }

    private MaterializedObject nextInChain;

    @Override
    public MaterializedObject getNextInChain() {
        return nextInChain;
    }

    @Override
    public void setNextInChain(MaterializedObject next) {
        this.nextInChain = next;
    }

    private static class DematerializedSkipList<E> implements DematerializedObject {
        private final MultiversionedHandle<ExampleSkipList<E>> handle;

        DematerializedSkipList(ExampleSkipList<E> e) {
            this.handle = e.handle;
        }

        @Override
        public MultiversionedHandle getHandle() {
            return handle;
        }

        @Override
        public MaterializedObject rematerialize(Transaction t) {
            return new ExampleSkipList(this, t);
        }
    }
}
