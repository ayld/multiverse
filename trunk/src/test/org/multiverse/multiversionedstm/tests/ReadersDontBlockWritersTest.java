package org.multiverse.multiversionedstm.tests;

import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.commit;
import org.multiverse.api.Originator;
import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.MultiversionedStm;
import org.multiverse.multiversionedstm.examples.IntegerValue;

public class ReadersDontBlockWritersTest {

    private MultiversionedStm stm;
    private Originator<IntegerValue> originator;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
        originator = commit(stm, new IntegerValue(0));
    }

    @Test
    public void test() {
        Transaction readTransaction = stm.startTransaction();
        IntegerValue readValue = readTransaction.read(originator);
        readValue.get();

        Transaction writeTransaction = stm.startTransaction();
        IntegerValue writtenValue = writeTransaction.read(originator);
        writtenValue.inc();
        writeTransaction.commit();
    }
}
