package org.multiverse.instrumentation;

import org.multiverse.api.Handle;
import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.MultiversionedStm;
import org.multiverse.tcollections.Latch;

/**
 * A Main that runs some tests.
 * <p/>
 * will be removed in the future..
 */
public class TestRunningMain {

    public static void main(String[] args) {
        System.out.println("Main called");
    }

    private static void testLatch() {
        Latch latch = new Latch();
        latch.open();
        System.out.println("original " + latch);

        MultiversionedStm stm = new MultiversionedStm();
        Transaction t = stm.startTransaction();
        Handle<Latch> handle = t.attach(latch);
        t.commit();

        Transaction t2 = stm.startTransaction();
        Latch found = t2.read(handle);
        System.out.println("found: " + found);
        t2.commit();
    }
}
