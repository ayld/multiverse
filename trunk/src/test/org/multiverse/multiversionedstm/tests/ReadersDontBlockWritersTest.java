package org.multiverse.multiversionedstm.tests;

import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.commit;
import org.multiverse.api.Handle;
import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.MultiversionedStm;
import org.multiverse.multiversionedstm.examples.ExampleIntValue;

public class ReadersDontBlockWritersTest {

    private MultiversionedStm stm;
    private Handle<ExampleIntValue> handle;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
        handle = commit(stm, new ExampleIntValue(0));
    }

    @Test
    public void test() {
        Transaction readTransaction = stm.startTransaction();
        ExampleIntValue readValue = readTransaction.read(handle);
        readValue.get();

        Transaction writeTransaction = stm.startTransaction();
        ExampleIntValue writtenValue = writeTransaction.read(handle);
        writtenValue.inc();
        writeTransaction.commit();
    }
}
