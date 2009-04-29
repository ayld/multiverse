package org.multiverse.examples;

import org.multiverse.api.LazyReference;
import org.multiverse.api.Originator;
import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.*;
import static org.multiverse.multiversionedstm.MultiversionedStmUtils.retry;

import java.util.ArrayList;
import java.util.List;

public final class Stack<E> implements MaterializedObject {

    private StackNode<E> head;

    public Stack() {
        this.originator = new DefaultOriginator<Stack<E>>();
    }

    public E peek() {
        return head == null ? null : head.getValue();
    }

    public boolean isEmpty() {
        ensureHeadLoaded();

        return head == null;
    }

    public void push(E item) {
        ensureHeadLoaded();

        head = new StackNode<E>(head, item);
    }

    public E pop() {
        ensureHeadLoaded();

        if (head == null)
            retry();

        return removeTopItem();
    }

    public E tryPop() {
        ensureHeadLoaded();

        if (head == null)
            return null;

        return removeTopItem();
    }

    private E removeTopItem() {
        StackNode<E> oldHead = head;
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
        ensureHeadLoaded();

        return head == null ? 0 : head.length();
    }

    /**
     * Returns a List representation of the array with the last pushed element as the head of the list.
     */
    public List<E> asList() {
        ensureHeadLoaded();
        List<E> result = new ArrayList<E>(size());
        StackNode<E> node = head;
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

        if (!(thatObj instanceof Stack))
            return false;

        Stack that = (Stack) thatObj;
        if (that.size() != this.size())
            return false;

        if (this.head == null)
            return that.head == null;

        return this.head.equals(that.head);
    }

    //================== generated ======================

    private DematerializedStack<E> lastDematerialized;
    private LazyReference<StackNode<E>> headRef;
    private final Originator<Stack<E>> originator;

    private Stack(DematerializedStack<E> dematerializedStack, Transaction t) {
        this.lastDematerialized = dematerializedStack;
        this.headRef = t.readLazyAndUnmanaged(dematerializedStack.headOriginator);
        this.originator = dematerializedStack.getOriginator();
    }

    @Override
    public Originator<Stack<E>> getOriginator() {
        return originator;
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
        if (head != null) memberTracer.onMember(head);
    }

    @Override
    public boolean isDirty() {
        if (lastDematerialized == null)
            return true;

        if (MultiversionedStmUtils.getOriginator(headRef, head) != lastDematerialized.headOriginator)
            return true;

        return false;
    }

    @Override
    public DematerializedStack<E> dematerialize() {
        return lastDematerialized = new DematerializedStack<E>(this);
    }

    private static class DematerializedStack<E> implements DematerializedObject {
        private final Originator<Stack<E>> originator;
        private final Originator<StackNode<E>> headOriginator;

        private DematerializedStack(Stack<E> stack) {
            this.originator = stack.getOriginator();
            this.headOriginator = MultiversionedStmUtils.getOriginator(stack.headRef, stack.head);
        }

        @Override
        public MaterializedObject rematerialize(Transaction t) {
            return new Stack<E>(this, t);
        }

        @Override
        public Originator<Stack<E>> getOriginator() {
            return originator;
        }
    }
}
