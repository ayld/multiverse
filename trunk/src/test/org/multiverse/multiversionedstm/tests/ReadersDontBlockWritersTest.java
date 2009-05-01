package org.multiverse.multiversionedstm.tests;

import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.commit;
import org.multiverse.api.Handle;
import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.MultiversionedStm;
import org.multiverse.multiversionedstm.examples.IntegerValue;

public class ReadersDontBlockWritersTest {

    private MultiversionedStm stm;
    private Handle<IntegerValue> handle;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
        handle = commit(stm, new IntegerValue(0));
    }

    @Test
    public void test() {
        Transaction readTransaction = stm.startTransaction();
        IntegerValue readValue = readTransaction.read(handle);
        readValue.get();

        Transaction writeTransaction = stm.startTransaction();
        IntegerValue writtenValue = writeTransaction.read(handle);
        writtenValue.inc();
        writeTransaction.commit();
    }
}
