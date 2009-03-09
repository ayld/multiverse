package org.codehaus.multiverse.multiversionedstm.tests;

import static org.codehaus.multiverse.TestUtils.commit;
import org.codehaus.multiverse.core.Transaction;
import org.codehaus.multiverse.multiversionedheap.standard.DefaultMultiversionedHeap;
import org.codehaus.multiverse.multiversionedstm.MultiversionedStm;
import org.codehaus.multiverse.multiversionedstm.examples.Reference;
import org.junit.Before;
import org.junit.Test;

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
        r2.set(A);
        t2.commit();

        r1.set(C);
        t1.commit();
    }
}
