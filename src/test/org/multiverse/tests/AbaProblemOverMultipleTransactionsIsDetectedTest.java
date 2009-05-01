package org.multiverse.tests;

import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.commit;
import org.multiverse.api.Originator;
import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.WriteConflictException;
import org.multiverse.multiversionedstm.MultiversionedStm;
import org.multiverse.multiversionedstm.examples.IntegerValue;

public class AbaProblemOverMultipleTransactionsIsDetectedTest {

    private static final int A = 1;
    private static final int B = 2;
    private static final int C = 3;

    private MultiversionedStm stm;
    private Originator<IntegerValue> originator;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
        originator = commit(stm, new IntegerValue(A));
    }

    @Test
    public void test() {
        Transaction t1 = stm.startTransaction();
        IntegerValue r1 = t1.read(originator);

        Transaction t2 = stm.startTransaction();
        IntegerValue r2 = t2.read(originator);
        r2.set(B);
        t2.commit();

        Transaction t3 = stm.startTransaction();
        IntegerValue r3 = t3.read(originator);
        r3.set(B);
        t3.commit();

        r1.set(C);
        try {
            t1.commit();
            fail();
        } catch (WriteConflictException er) {

        }
    }
}
