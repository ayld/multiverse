package org.codehaus.multiverse.multiversionedstm.performance;

import com.sun.japex.JapexDriverBase;
import com.sun.japex.TestCase;
import static org.codehaus.multiverse.TestUtils.atomicInsert;
import org.codehaus.multiverse.core.Transaction;
import org.codehaus.multiverse.multiversionedstm.MultiversionedStm;
import org.codehaus.multiverse.multiversionedstm.examples.IntegerValue;

/**
 * Multiverse doesn't instrument reads/writes of normal fields, so  the performance of reads and writes is not
 * impacted and the compiler can do all his tricks to make code faster. This tests makes sure that the reads and
 * writes are f*cking fast. On my dual core laptop, the test runs in a few hundred ms.
 *
 * @author Peter Veentjer.
 */
public class AccessingFieldPerformanceDriver extends JapexDriverBase {

    private MultiversionedStm stm;
    private long handle;
    private int iterations = Integer.MAX_VALUE;

    @Override
    public void prepare(TestCase testCase) {
        readParams(testCase);
        stm = new MultiversionedStm();
        setUpStructures();
    }

    private void setUpStructures() {
        handle = atomicInsert(stm, new IntegerValue());
    }

    private void readParams(TestCase testCase) {
        iterations = testCase.getIntParam("iterations");
    }

    public void run(TestCase testCase) {
        Transaction t = stm.startTransaction();
        IntegerValue value = (IntegerValue) t.read(handle);
        for (int k = 0; k < iterations; k++)
            value.inc();
        t.commit();

        //long endNs = System.nanoTime();

        //double transactionPerSec = (1.0 * TimeUnit.SECONDS.toNanos(1) * iterations) / (endNs - startNs);
        //System.out.printf("Performance is %s inc()/second\n", transactionPerSec);
    }
}
