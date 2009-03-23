package org.codehaus.multiverse.multiversionedstm.tests;

import static org.codehaus.multiverse.TestUtils.commit;
import org.codehaus.multiverse.api.Transaction;
import org.codehaus.multiverse.multiversionedstm.MultiversionedStm;
import org.codehaus.multiverse.multiversionedstm.examples.IntegerValue;
import org.junit.Before;
import org.junit.Test;

public class ReadersDontBlockWritersTest {

    private MultiversionedStm stm;
    private long handle;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
        handle = commit(stm, new IntegerValue(0));
    }

    @Test
    public void test() {
        Transaction readTransaction = stm.startTransaction();
        IntegerValue readValue = (IntegerValue) readTransaction.read(handle);
        readValue.get();

        Transaction writeTransaction = stm.startTransaction();
        IntegerValue writtenValue = (IntegerValue) writeTransaction.read(handle);
        writtenValue.inc();
        writeTransaction.commit();
    }
}
