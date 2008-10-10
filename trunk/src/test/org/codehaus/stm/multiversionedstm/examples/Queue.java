package org.codehaus.stm.multiversionedstm.examples;

import org.codehaus.stm.multiversionedstm.Citizen;
import org.codehaus.stm.multiversionedstm.DehydratedCitizen;
import org.codehaus.stm.multiversionedstm.MultiversionedStm;

import java.util.Iterator;
import java.util.LinkedList;

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

    //================== generated =================

    private long ptr;
    private MultiversionedStm.MultiversionedTransaction transaction;

    public void ___onAttach(MultiversionedStm.MultiversionedTransaction transaction) {
        this.transaction = transaction;
    }

    public MultiversionedStm.MultiversionedTransaction ___getTransaction() {
        return transaction;
    }

    public Iterator<Citizen> ___findNewlyborns() {
        LinkedList result = new LinkedList();
        result.add(readyToPopStack);
        result.add(pushedStack);
        return result.iterator();
    }

    public long ___getPointer() {
        return ptr;
    }

    public void ___setPointer(long ptr) {
        this.ptr = ptr;
    }

    public DehydratedCitizen ___hydrate() {
        return new DehydratedQueue(this);
    }

    public boolean ___isDirty() {
        return false;
    }

    public static class DehydratedQueue implements DehydratedCitizen {
        private final long readyToPopStackPtr;
        private final long pushedStackPtr;

        DehydratedQueue(Queue queue) {
            this.readyToPopStackPtr = queue.readyToPopStack.___getPointer();
            this.pushedStackPtr = queue.pushedStack.___getPointer();
        }

        public Queue hydrate(long ptr, MultiversionedStm.MultiversionedTransaction transaction) {
            Queue queue = new Queue();
            queue.ptr = ptr;
            queue.transaction = transaction;
            queue.readyToPopStack = (Stack) transaction.readRoot(readyToPopStackPtr);
            queue.pushedStack = (Stack) transaction.readRoot(pushedStackPtr);
            return queue;
        }
    }
}
