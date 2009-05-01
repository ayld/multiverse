package org.multiverse.multiversionedstm.tests;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.commit;
import org.multiverse.api.Originator;
import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.MultiversionedStm;
import org.multiverse.multiversionedstm.examples.IntegerValue;

public class WritersDontBlockReadersTest {
    private MultiversionedStm stm;
    private Originator<IntegerValue> originator;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
        originator = commit(stm, new IntegerValue());
    }

    @Test
    public void test() {
        Transaction writeTransaction = stm.startTransaction();
        IntegerValue writtenValue = writeTransaction.read(originator);
        writtenValue.inc();

        Transaction readTransaction = stm.startTransaction();
        IntegerValue readValue = readTransaction.read(originator);
        int value = readValue.get();
        readTransaction.commit();

        assertEquals(0, value);
    }
}
