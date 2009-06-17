package org.multiverse.multiversionedstm.tests;

import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.commit;
import org.multiverse.api.Handle;
import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.WriteConflictException;
import org.multiverse.multiversionedstm.MultiversionedStm;
import org.multiverse.multiversionedstm.examples.ExampleIntValue;

public class AbaProblemOverMultipleTransactionsIsDetectedTest {

    private static final int A = 1;
    private static final int B = 2;
    private static final int C = 3;

    private MultiversionedStm stm;
    private Handle<ExampleIntValue> handle;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
        handle = commit(stm, new ExampleIntValue(A));
    }

    @Test
    public void test() {
        Transaction t1 = stm.startTransaction();
        ExampleIntValue r1 = t1.read(handle);

        Transaction t2 = stm.startTransaction();
        ExampleIntValue r2 = t2.read(handle);
        r2.set(B);
        t2.commit();

        Transaction t3 = stm.startTransaction();
        ExampleIntValue r3 = t3.read(handle);
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