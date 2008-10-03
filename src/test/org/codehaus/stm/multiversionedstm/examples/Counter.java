package org.codehaus.stm.multiversionedstm.examples;

import org.codehaus.stm.multiversionedstm.Citizen;
import org.codehaus.stm.multiversionedstm.HydratedCitizen;
import org.codehaus.stm.multiversionedstm.MultiversionedStm;
import org.codehaus.stm.util.EmptyIterator;

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

    private HydratedCounter initialHydratedCounter;
    private MultiversionedStm.MultiversionedTransaction transaction;
    private long ptr;

    public void ___onAttach(MultiversionedStm.MultiversionedTransaction transaction) {
        this.transaction = transaction;
    }

    public MultiversionedStm.MultiversionedTransaction ___getTransaction() {
        return transaction;
    }

    public Iterator<Citizen> ___findNewlyborns() {
        return EmptyIterator.INSTANCE;
    }

    public long ___getPointer() {
        return ptr;
    }

    public HydratedCitizen ___hydrate() {
        return new HydratedCounter(this);
    }

    public boolean ___isDirty() {
        return count != initialHydratedCounter.count;
    }

    public void ___setPointer(long ptr) {
        this.ptr = ptr;
    }

    public static class HydratedCounter implements HydratedCitizen {

        private final long count;

        private HydratedCounter(Counter counter) {
            this.count = counter.count;
        }

        public Citizen dehydrate(long ptr, MultiversionedStm.MultiversionedTransaction transaction) {
            Counter counter = new Counter();
            counter.ptr = ptr;
            counter.transaction = transaction;
            counter.count = count;
            counter.initialHydratedCounter = this;
            return counter;
        }
    }
}
