package org.multiverse.stms.alpha.manualinstrumentation;

import org.multiverse.api.annotations.AtomicMethod;

public final class IntQueue {

    private final IntStack pushedStack;
    private final IntStack readyToPopStack;
    private final int maxCapacity;

    public IntQueue() {
        this(Integer.MAX_VALUE);
    }

    public IntQueue(int maxCapacity) {
        if (maxCapacity < 0) {
            throw new IllegalArgumentException();
        }
        pushedStack = new IntStack();
        readyToPopStack = new IntStack();
        this.maxCapacity = maxCapacity;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    @AtomicMethod
    public void push(final int item) {
        if (size() > maxCapacity) {
            throw new IllegalStateException();
        }

        pushedStack.push(item);
    }

    @AtomicMethod
    public int pop() {
        if (readyToPopStack.isEmpty()) {
            while (!pushedStack.isEmpty()) {
                readyToPopStack.push(pushedStack.pop());
            }
        }

        return readyToPopStack.pop();
    }

    @AtomicMethod
    public int size() {
        return pushedStack.size() + readyToPopStack.size();
    }

    @AtomicMethod
    public boolean isEmpty() {
        return pushedStack.isEmpty() && readyToPopStack.isEmpty();
    }
}
