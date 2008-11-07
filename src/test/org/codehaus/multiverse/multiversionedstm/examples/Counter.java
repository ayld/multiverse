package org.codehaus.multiverse.multiversionedstm.examples;

import org.codehaus.multiverse.multiversionedstm.Citizen;
import org.codehaus.multiverse.multiversionedstm.DehydratedCitizen;
import org.codehaus.multiverse.multiversionedstm.MultiversionedStm;
import org.codehaus.multiverse.util.EmptyIterator;

import java.util.Iterator;

public class Counter implements Citizen {

    private long count;

    public long get() {
        return count;
    }

    public long incAndGet() {
        count++;
        return count;
    }

    public long getAndInc() {
        long oldCount = count;
        count++;
        return oldCount;
    }

    public void inc() {
        count++;
    }

    //============ generated ==============================

    private DehydratedCounter initialHydratedCounter;
    private MultiversionedStm.MultiversionedTransaction transaction;
    private long ptr;

    public void ___onAttach(MultiversionedStm.MultiversionedTransaction transaction) {
        this.transaction = transaction;
    }

    public MultiversionedStm.MultiversionedTransaction ___getTransaction() {
        return transaction;
    }

    public Iterator<Citizen> ___directReachableIterator() {
        return EmptyIterator.INSTANCE;
    }

    public long ___getPointer() {
        return ptr;
    }

    public DehydratedCitizen ___dehydrate() {
        return new DehydratedCounter(this);
    }

    public boolean ___isDirty() {
        return count != initialHydratedCounter.count;
    }

    public void ___setPointer(long ptr) {
        this.ptr = ptr;
    }

    public static class DehydratedCounter implements DehydratedCitizen {

        private final long count;

        private DehydratedCounter(Counter counter) {
            this.count = counter.count;
        }

        public Citizen hydrate(long ptr, MultiversionedStm.MultiversionedTransaction transaction) {
            Counter counter = new Counter();
            counter.ptr = ptr;
            counter.transaction = transaction;
            counter.count = count;
            counter.initialHydratedCounter = this;
            return counter;
        }
    }
}
