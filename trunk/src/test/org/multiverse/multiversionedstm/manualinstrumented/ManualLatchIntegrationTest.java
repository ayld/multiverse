package org.multiverse.multiversionedstm.manualinstrumented;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Handle;
import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.MultiversionedStm;

public class ManualLatchIntegrationTest {
    private MultiversionedStm stm;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
    }

    @Test
    public void testRematerialize() {
        ManualLatch latch = new ManualLatch();

        Transaction t = stm.startTransaction();
        Handle<ManualLatch> handle = t.attach(latch);
        t.commit();

        Transaction t2 = stm.startTransaction();
        ManualLatch found = t2.read(handle);
        assertEquals(latch.isOpen(), found.isOpen());
    }
}