package org.multiverse.multiversionedstm.tests;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.commit;
import org.multiverse.api.Handle;
import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.MultiversionedStm;
import org.multiverse.multiversionedstm.examples.IntegerValue;

public class WritersDontBlockReadersTest {
    private MultiversionedStm stm;
    private Handle<IntegerValue> handle;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
        handle = commit(stm, new IntegerValue());
    }

    @Test
    public void test() {
        Transaction writeTransaction = stm.startTransaction();
        IntegerValue writtenValue = writeTransaction.read(handle);
        writtenValue.inc();

        Transaction readTransaction = stm.startTransaction();
        IntegerValue readValue = readTransaction.read(handle);
        int value = readValue.get();
        readTransaction.commit();

        assertEquals(0, value);
    }
}
