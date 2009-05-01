package org.multiverse.multiversionedstm.examples;

import org.multiverse.api.Handle;
import org.multiverse.api.LazyReference;
import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.*;

public final class StackNode<E> implements MaterializedObject {
    private StackNode<E> next;
    private final E value;
    private final int length;

    public StackNode(StackNode<E> next, E value) {
        this.handle = new DefaultHandle<StackNode<E>>();
        this.next = next;
        this.value = value;
        this.length = next == null ? 1 : next.length + 1;
    }

    public StackNode<E> getNext() {
        if (nextRef != null) {
            next = nextRef.get();
            nextRef = null;
        }
        return next;
    }

    public int length() {
        return length;
    }

    public E getValue() {
        return value;
    }

    // ============================== generated ========================

    private DematerializedNode<E> lastDematerialized;
    private LazyReference<StackNode<E>> nextRef;
    private Handle<StackNode<E>> handle;

    private StackNode(DematerializedNode<E> dematerializedNode, Transaction t) {
        this.lastDematerialized = dematerializedNode;
        this.handle = dematerializedNode.getHandle();
        this.value = dematerializedNode.value instanceof Handle ? (E) t.read((Handle) dematerializedNode.value) : (E) dematerializedNode.value;
        this.length = dematerializedNode.length;
        this.nextRef = t.readLazyAndUnmanaged(dematerializedNode.nextHandle);
    }

    @Override
    public Handle<StackNode<E>> getHandle() {
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
        private final Handle<StackNode<E>> handle;
        private final Object value;
        private final int length;
        private final Handle<StackNode<E>> nextHandle;

        public DematerializedNode(StackNode<E> node) {
            this.handle = node.getHandle();
            this.value = node.value instanceof MaterializedObject ? ((MaterializedObject) node.value).getHandle() : node.value;
            this.length = node.length;
            this.nextHandle = MultiversionedStmUtils.getHandle(node.nextRef, node.next);
        }

        @Override
        public MaterializedObject rematerialize(Transaction t) {
            return new StackNode<E>(this, t);
        }

        @Override
        public Handle<StackNode<E>> getHandle() {
            return handle;
        }
    }
}
