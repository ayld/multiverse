package org.multiverse.multiversionedstm.examples;

import org.multiverse.api.LazyReference;
import org.multiverse.api.StmUtils;
import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.*;

import java.util.ArrayList;
import java.util.List;

public final class ExampleStack<E> implements MaterializedObject {

    public ExampleStackNode<E> head;
    public int size;

    public ExampleStack() {
        this.handle = new DefaultMultiversionedHandle<ExampleStack<E>>();
    }

    public E peek() {
        ensureHeadLoaded();

        return head == null ? null : head.getValue();
    }

    public boolean isEmpty() {
        ensureHeadLoaded();

        return head == null;
    }

    public void deleteMe(ExampleStackNode<E> x) {
        headRef = null;
        head = x;
    }

    public void push(E item) {
        ensureHeadLoaded();
        head = new ExampleStackNode<E>(head, item);
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
        ExampleStackNode<E> oldHead = head;
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
        ExampleStackNode<E> node = head;
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

        if (!(thatObj instanceof ExampleStack))
            return false;

        ExampleStack that = (ExampleStack) thatObj;
        if (that.size() != this.size())
            return false;

        if (this.head == null)
            return that.head == null;

        return this.head.equals(that.head);
    }

    //================== generated ======================

    private DematerializedStack<E> lastDematerialized;
    private LazyReference<ExampleStackNode<E>> headRef;
    private final MultiversionedHandle<ExampleStack<E>> handle;

    private ExampleStack(DematerializedStack<E> dematerializedStack, Transaction t) {
        this.handle = dematerializedStack.getHandle();
        this.lastDematerialized = dematerializedStack;
        this.headRef = t.readLazyAndUnmanaged(dematerializedStack.head);
        this.size = dematerializedStack.size;
    }

    @Override
    public MultiversionedHandle<ExampleStack<E>> getHandle() {
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
        private final MultiversionedHandle<ExampleStack<E>> handle;
        private final MultiversionedHandle<ExampleStackNode<E>> head;
        private final int size;

        private DematerializedStack(ExampleStack<E> stack) {
            this.handle = stack.getHandle();
            this.head = MultiversionedStmUtils.getHandle(stack.headRef, stack.head);
            this.size = stack.size;
        }

        @Override
        public MaterializedObject rematerialize(Transaction t) {
            return new ExampleStack<E>(this, t);
        }

        @Override
        public MultiversionedHandle<ExampleStack<E>> getHandle() {
            return handle;
        }
    }
}
