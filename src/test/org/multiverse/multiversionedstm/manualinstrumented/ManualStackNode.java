package org.multiverse.multiversionedstm.manualinstrumented;

import org.multiverse.api.Handle;
import org.multiverse.api.LazyReference;
import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.*;

public final class ManualStackNode<E> implements MaterializedObject {
    private ManualStackNode<E> next;
    private final E value;

    public ManualStackNode(ManualStackNode<E> next, E value) {
        this.handle = new DefaultMultiversionedHandle<ManualStackNode<E>>();
        this.next = next;
        this.value = value;
    }

    public ManualStackNode<E> getNext() {
        if (nextRef != null) {
            next = nextRef.get();
            nextRef = null;
        }
        return next;
    }

    public E getValue() {
        return value;
    }

    // ============================== generated ========================

    private DematerializedNode<E> lastDematerialized;
    private LazyReference<ManualStackNode<E>> nextRef;
    private MultiversionedHandle<ManualStackNode<E>> handle;

    private ManualStackNode(DematerializedNode<E> dematerializedNode, Transaction t) {
        this.lastDematerialized = dematerializedNode;
        this.handle = dematerializedNode.getHandle();
        this.value = dematerializedNode.value instanceof Handle ? (E) t.read((Handle) dematerializedNode.value) : (E) dematerializedNode.value;
        this.nextRef = t.readLazyAndSelfManaged(dematerializedNode.next);
    }

    @Override
    public MultiversionedHandle<ManualStackNode<E>> getHandle() {
        return handle;
    }

    @Override
    public boolean isDirty() {
        if (lastDematerialized == null)
            return true;

        return false;
    }

    @Override
    public DematerializedNode<E> dematerialize() {
        return lastDematerialized = new DematerializedNode<E>(this);
    }

    private MaterializedObject nextInChain;

    @Override
    public MaterializedObject getNextInChain() {
        return nextInChain;
    }

    @Override
    public void setNextInChain(MaterializedObject nextInChain) {
        this.nextInChain = nextInChain;
    }

    @Override
    public void walkMaterializedMembers(MemberWalker memberWalker) {
        if (value instanceof MaterializedObject) memberWalker.onMember((MaterializedObject) value);
        if (next != null) memberWalker.onMember(next);
    }

    public static class DematerializedNode<E> implements DematerializedObject {
        private final MultiversionedHandle<ManualStackNode<E>> handle;
        private final Object value;
        private final MultiversionedHandle<ManualStackNode<E>> next;

        public DematerializedNode(ManualStackNode<E> node) {
            this.handle = node.handle;
            this.value = node.value instanceof MaterializedObject ? ((MaterializedObject) node.value).getHandle() : node.value;
            this.next = MultiversionedStmUtils.getHandle(node.nextRef, node.next);
        }

        @Override
        public MaterializedObject rematerialize(Transaction t) {
            return new ManualStackNode<E>(this, t);
        }

        @Override
        public MultiversionedHandle<ManualStackNode<E>> getHandle() {
            return handle;
        }
    }
}
