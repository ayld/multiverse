package org.multiverse.multiversionedstm.manualinstrumented;

import org.multiverse.api.LazyReference;
import org.multiverse.api.StmUtils;
import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.*;

import java.util.ArrayList;
import java.util.List;

public final class ManualStack<E> implements MaterializedObject {

    public ManualStackNode<E> head;
    public int size;

    public ManualStack() {
        this.handle = new DefaultMultiversionedHandle<ManualStack<E>>();
    }

    public E peek() {
        ensureHeadLoaded();

        return head == null ? null : head.getValue();
    }

    public boolean isEmpty() {
        ensureHeadLoaded();

        return head == null;
    }

    public void deleteMe(ManualStackNode<E> x) {
        headRef = null;
        head = x;
    }

    public void push(E item) {
        ensureHeadLoaded();
        head = new ManualStackNode<E>(head, item);
        size++;
    }

    public E pop() {
        ensureHeadLoaded();

        if (head == null)
            StmUtils.retry();

        return removeTopItem();
    }

    public E tryPop() {
        ensureHeadLoaded();

        if (head == null)
            return null;

        return removeTopItem();
    }

    private E removeTopItem() {
        size--;
        ManualStackNode<E> oldHead = head;
        head = oldHead.getNext();
        return oldHead.getValue();
    }

    private void ensureHeadLoaded() {
        if (headRef != null) {
            head = headRef.get();
            headRef = null;
        }
    }

    public int size() {
        return size;
    }

    /**
     * Returns a List representation of the array with the last pushed element as the head of the list.
     */
    public List<E> asList() {
        ensureHeadLoaded();
        List<E> result = new ArrayList<E>(size());
        ManualStackNode<E> node = head;
        for (int k = 0; k < size(); k++) {
            result.add(node.getValue());
            node = node.getNext();
        }
        return result;
    }

    @Override
    public String toString() {
        return asList().toString();
    }

    @Override
    public int hashCode() {
        return asList().hashCode();
    }

    @Override
    public boolean equals(Object thatObj) {
        if (thatObj == this)
            return true;

        if (!(thatObj instanceof ManualStack))
            return false;

        ManualStack that = (ManualStack) thatObj;
        if (that.size() != this.size())
            return false;

        if (this.head == null)
            return that.head == null;

        return this.head.equals(that.head);
    }

    //================== generated ======================

    private DematerializedStack<E> lastDematerialized;
    private LazyReference<ManualStackNode<E>> headRef;
    private final MultiversionedHandle<ManualStack<E>> handle;

    private ManualStack(DematerializedStack<E> dematerializedStack, Transaction t) {
        this.handle = dematerializedStack.getHandle();
        this.lastDematerialized = dematerializedStack;
        this.headRef = t.readLazyAndSelfManaged(dematerializedStack.head);
        this.size = dematerializedStack.size;
    }

    @Override
    public MultiversionedHandle<ManualStack<E>> getHandle() {
        return handle;
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
        if (head != null) memberWalker.onMember(head);
    }

    @Override
    public boolean isDirty() {
        if (lastDematerialized == null)
            return true;

        if (MultiversionedStmUtils.getHandle(headRef, head) != lastDematerialized.head)
            return true;

        return false;
    }

    @Override
    public DematerializedStack<E> dematerialize() {
        return lastDematerialized = new DematerializedStack<E>(this);
    }

    public static class DematerializedStack<E> implements DematerializedObject {
        private final MultiversionedHandle<ManualStack<E>> handle;
        private final MultiversionedHandle<ManualStackNode<E>> head;
        private final int size;

        private DematerializedStack(ManualStack<E> stack) {
            this.handle = stack.getHandle();
            this.head = MultiversionedStmUtils.getHandle(stack.headRef, stack.head);
            this.size = stack.size;
        }

        @Override
        public MaterializedObject rematerialize(Transaction t) {
            return new ManualStack<E>(this, t);
        }

        @Override
        public MultiversionedHandle<ManualStack<E>> getHandle() {
            return handle;
        }
    }
}
