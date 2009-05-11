package org.multiverse.multiversionedstm.examples;

import org.multiverse.api.Handle;
import org.multiverse.api.LazyReference;
import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.*;

public final class ExampleStackNode<E> implements MaterializedObject {
    private ExampleStackNode<E> next;
    private final E value;

    public ExampleStackNode(ExampleStackNode<E> next, E value) {
        this.handle = new DefaultMultiversionedHandle<ExampleStackNode<E>>();
        this.next = next;
        this.value = value;
    }

    public ExampleStackNode<E> getNext() {
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
    private LazyReference<ExampleStackNode<E>> nextRef;
    private MultiversionedHandle<ExampleStackNode<E>> handle;

    private ExampleStackNode(DematerializedNode<E> dematerializedNode, Transaction t) {
        this.lastDematerialized = dematerializedNode;
        this.handle = dematerializedNode.getHandle();
        this.value = dematerializedNode.value instanceof Handle ? (E) t.read((Handle) dematerializedNode.value) : (E) dematerializedNode.value;
        this.nextRef = t.readLazyAndUnmanaged(dematerializedNode.next);
    }

    @Override
    public MultiversionedHandle<ExampleStackNode<E>> getHandle() {
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
        private final MultiversionedHandle<ExampleStackNode<E>> handle;
        private final Object value;
        private final MultiversionedHandle<ExampleStackNode<E>> next;

        public DematerializedNode(ExampleStackNode<E> node) {
            this.handle = node.handle;
            this.value = node.value instanceof MaterializedObject ? ((MaterializedObject) node.value).getHandle() : node.value;
            this.next = MultiversionedStmUtils.getHandle(node.nextRef, node.next);
        }

        @Override
        public MaterializedObject rematerialize(Transaction t) {
            return new ExampleStackNode<E>(this, t);
        }

        @Override
        public MultiversionedHandle<ExampleStackNode<E>> getHandle() {
            return handle;
        }
    }
}
