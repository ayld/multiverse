package org.codehaus.multiverse.multiversionedstm.tests;

import static org.codehaus.multiverse.TestUtils.commit;
import org.codehaus.multiverse.api.Transaction;
import org.codehaus.multiverse.api.exceptions.WriteConflictError;
import org.codehaus.multiverse.multiversionedheap.standard.DefaultMultiversionedHeap;
import org.codehaus.multiverse.multiversionedstm.MultiversionedStm;
import org.codehaus.multiverse.multiversionedstm.examples.Reference;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

public class AbaProblemOverMultipleTransactionIsDetectedTest {

    private static final String A = "A";
    private static final String B = "B";
    private static final String C = "C";

    private MultiversionedStm stm;
    private long handle;
    private DefaultMultiversionedHeap heap;

    @Before
    public void setUp() {
        heap = new DefaultMultiversionedHeap();
        stm = new MultiversionedStm(heap);
        handle = commit(stm, new Reference(A));
    }

    @Test
    public void test() {
        Transaction t1 = stm.startTransaction();
        Reference<String> r1 = (Reference<String>) t1.read(handle);

        Transaction t2 = stm.startTransaction();
        Reference<String> r2 = (Reference<String>) t2.read(handle);
        r2.set(B);
        t2.commit();

        Transaction t3 = stm.startTransaction();
        Reference<String> r3 = (Reference<String>) t3.read(handle);
        r3.set(B);
        t3.commit();

        r1.set(C);
        try {
            t1.commit();
            fail();
        } catch (WriteConflictError er) {

        }
    }
}
