package org.multiverse.stms.alpha.instrumentation.integrationtest;

import org.multiverse.api.annotations.AtomicMethod;
import org.multiverse.api.annotations.AtomicObject;
import org.multiverse.stms.alpha.manualinstrumentation.Stack;

/**
 * @author Peter Veentjer
 */
@AtomicObject
public class Queue<E> {

    public final Stack<E> pushedStack;
    public final Stack<E> readyToPopStack;
    public final int maxCapacity;


    public Queue(int maxCapacity) {
        if (maxCapacity < 0) {
            throw new IllegalArgumentException();
        }
        pushedStack = new Stack<E>();
        readyToPopStack = new Stack<E>();
        this.maxCapacity = maxCapacity;
    }

    //this calling constructor is not allowed yet.
    public Queue() {
        pushedStack = new Stack<E>();
        readyToPopStack = new Stack<E>();
        maxCapacity = Integer.MAX_VALUE;
    }

    @AtomicMethod(readonly = true)
    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void push(E item) {
        if (size() > maxCapacity) {
            throw new IllegalStateException();
        }

        pushedStack.push(item);
    }

    public E take() {
        if (readyToPopStack.isEmpty()) {
            while (!pushedStack.isEmpty()) {
                readyToPopStack.push(pushedStack.pop());
            }
        }

        return readyToPopStack.pop();
    }

    @AtomicMethod(readonly = true)
    public int size() {
        return pushedStack.size() + readyToPopStack.size();
    }

    @AtomicMethod(readonly = true)
    public boolean isEmpty() {
        return pushedStack.isEmpty() && readyToPopStack.isEmpty();
    }
}
