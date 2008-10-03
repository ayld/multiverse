package org.codehaus.stm.multiversionedstm2.examples;

import org.codehaus.stm.multiversionedstm2.Citizen;
import org.codehaus.stm.multiversionedstm2.DehydratedCitizen;
import org.codehaus.stm.multiversionedstm2.MultiversionedStm;


public class Queue<E> implements Citizen {

    private Stack<E> readyToPopStack = new Stack<E>();
    private Stack<E> pushedStack = new Stack<E>();

    public E pop() {
        if (!readyToPopStack.isEmpty())
            return readyToPopStack.pop();

        while (!pushedStack.isEmpty()) {
            E item = pushedStack.pop();
            readyToPopStack.push(item);
        }

        return readyToPopStack.pop();
    }

    public void push(E value) {
        pushedStack.push(value);
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public int size() {
        return readyToPopStack.size() + pushedStack.size();
    }

    // ================== generated ====================

    private DehydratedQueue initial;

    public boolean isDirty() {
        return readyToPopStack.isDirty() || pushedStack.isDirty();
    }

    public DehydratedQueue getInitial() {
        return initial;
    }

    public DehydratedQueue dehydrate() {
        Stack.DehydratedStack dehydratedReadyToPopStack = readyToPopStack.dehydrate();
        Stack.DehydratedStack dehydratedPushedStack = pushedStack.dehydrate();
        if (dehydratedReadyToPopStack == initial.dehydratedReadyToPopStack &&
                dehydratedPushedStack == initial.dehydratedPushedStack) {
            return initial;
        }

        return new DehydratedQueue(dehydratedReadyToPopStack, dehydratedPushedStack);
    }

    static class DehydratedQueue extends DehydratedCitizen {
        final Stack.DehydratedStack dehydratedReadyToPopStack;
        final Stack.DehydratedStack dehydratedPushedStack;

        DehydratedQueue(Stack.DehydratedStack dehydratedReadyToPopStack, Stack.DehydratedStack dehydratedPushedStack) {
            this.dehydratedReadyToPopStack = dehydratedReadyToPopStack;
            this.dehydratedPushedStack = dehydratedPushedStack;
        }

        public Queue hydrate(MultiversionedStm.MultiversionedTransaction transaction) {
            Queue queue = new Queue();
            queue.initial = this;
            queue.pushedStack = (Stack) transaction.hydrate(dehydratedPushedStack);
            queue.readyToPopStack = (Stack) transaction.hydrate(dehydratedReadyToPopStack);
            return queue;
        }

        public long getVersion() {
            return 0;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}
