package org.multiverse.instrumentation;

import org.multiverse.api.Handle;
import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.MultiversionedStm;
import org.multiverse.tmutils.TmLatch;

/**
 * A Main that runs some tests.
 * <p/>
 * will be removed in the future..
 */
public class TestRunningMain {

    public static void main(String[] args) {
        System.out.println("BenchmarkMain called");
    }

    private static void testLatch() {
        TmLatch latch = new TmLatch();
        latch.open();
        System.out.println("original " + latch);

        MultiversionedStm stm = new MultiversionedStm();
        Transaction t = stm.startTransaction();
        Handle<TmLatch> handle = t.attach(latch);
        t.commit();

        Transaction t2 = stm.startTransaction();
        TmLatch found = t2.read(handle);
        System.out.println("found: " + found);
        t2.commit();
    }
}
