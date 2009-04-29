package org.multiverse.examples;

import org.multiverse.api.LazyReference;
import org.multiverse.api.Originator;
import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.*;

public final class StackNode<E> implements MaterializedObject {
    private StackNode<E> next;
    private final E value;
    private final int length;

    public StackNode(StackNode<E> next, E value) {
        this.originator = new DefaultOriginator<StackNode<E>>();
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
    private Originator<StackNode<E>> originator;

    private StackNode(DematerializedNode<E> dematerializedNode, Transaction t) {
        this.lastDematerialized = dematerializedNode;
        this.originator = dematerializedNode.getOriginator();
        this.value = dematerializedNode.value instanceof Originator ? (E) t.read((Originator) dematerializedNode.value) : (E) dematerializedNode.value;
        this.length = dematerializedNode.length;
        this.nextRef = t.readLazy(dematerializedNode.nextOriginator);
    }

    @Override
    public Originator<StackNode<E>> getOriginator() {
        return originator;
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
    public void memberTrace(MemberTracer memberTracer) {
        if (value instanceof MaterializedObject) memberTracer.onMember((MaterializedObject) value);
        if (next != null) memberTracer.onMember(next);
    }

    public static class DematerializedNode<E> implements DematerializedObject {
        private final Originator<StackNode<E>> originator;
        private final Object value;
        private final int length;
        private final Originator<StackNode<E>> nextOriginator;

        public DematerializedNode(StackNode<E> node) {
            this.originator = node.getOriginator();
            this.value = node.value instanceof MaterializedObject ? ((MaterializedObject) node.value).getOriginator() : node.value;
            this.length = node.length;
            this.nextOriginator = MultiversionedStmUtils.getOriginator(node.nextRef, node.next);
        }

        @Override
        public MaterializedObject rematerialize(Transaction t) {
            return new StackNode<E>(this, t);
        }

        @Override
        public Originator<StackNode<E>> getOriginator() {
            return originator;
        }
    }
}
