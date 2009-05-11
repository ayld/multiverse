package org.multiverse.multiversionedstm.tests;

import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.commit;
import org.multiverse.api.Handle;
import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.MultiversionedStm;
import org.multiverse.multiversionedstm.examples.ExampleIntegerValue;

/**
 * A test that shows that the ABA problem is not detected.
 * <p/>
 * The ABA problem occurrs when the following sequence happens:
 * <ol>
 * <li>a transaction reads a value "A" and doesn't commit yet</li>
 * <li>a second transaction udates the value to "B"<li>
 * <li>a third transaction updates the value to "A"</li>
 * <li>the first transaction commits</li>
 * </ol>
 * Since the value is still the same from the perspective of the first transaction (it has changed from
 * "A" to "B" back to "A" again) the question remains what to do. Should to be a problem or not.
 * <p/>
 * The stm object do dirty checks, so the second transaction that does the A->B and B-A doesn't write
 * any change because reference is not marked as dirty.
 * <p/>
 * For more information see:
 * http://en.wikipedia.org/wiki/ABA_problem
 *
 * @author Peter Veentjer.
 */
public class AbaProblemIsNotDetectedTest {
    private static final int A = 1;
    private static final int B = 2;
    private static final int C = 3;

    private MultiversionedStm stm;
    private Handle<ExampleIntegerValue> handle;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
        handle = commit(stm, new ExampleIntegerValue(A));
    }

    @Test
    public void test() {
        Transaction t1 = stm.startTransaction();
        ExampleIntegerValue r1 = t1.read(handle);

        Transaction t2 = stm.startTransaction();
        ExampleIntegerValue r2 = t2.read(handle);
        r2.set(B);
        r2.set(A);
        t2.commit();

        r1.set(C);
        t1.commit();
    }
}
