package org.multiverse.multiversionedstm.examples;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Handle;
import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.MultiversionedStm;

public class LatchIntegrationTest {
    private MultiversionedStm stm;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
    }

    @Test
    public void testRematerialize() {
        Latch latch = new Latch();

        Transaction t = stm.startTransaction();
        Handle<Latch> handle = t.attach(latch);
        t.commit();

        Transaction t2 = stm.startTransaction();
        Latch found = t2.read(handle);
        assertEquals(latch.isOpen(), found.isOpen());
    }
}