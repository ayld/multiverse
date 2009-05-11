package org.multiverse.multiversionedstm.examples;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Handle;
import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.MultiversionedStm;

public class ExampleLatchIntegrationTest {
    private MultiversionedStm stm;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
    }

    @Test
    public void testRematerialize() {
        ExampleLatch latch = new ExampleLatch();

        Transaction t = stm.startTransaction();
        Handle<ExampleLatch> handle = t.attach(latch);
        t.commit();

        Transaction t2 = stm.startTransaction();
        ExampleLatch found = t2.read(handle);
        assertEquals(latch.isOpen(), found.isOpen());
    }
}