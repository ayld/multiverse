package org.codehaus.multiverse.multiversionedstm.performance;

import com.sun.japex.Constants;
import com.sun.japex.JapexDriverBase;
import com.sun.japex.TestCase;
import static org.codehaus.multiverse.TestUtils.commit;
import org.codehaus.multiverse.multiversionedheap.HeapSnapshot;
import org.codehaus.multiverse.multiversionedheap.MultiversionedHeap.CommitResult;
import org.codehaus.multiverse.multiversionedheap.standard.DefaultMultiversionedHeap;
import org.codehaus.multiverse.multiversionedstm.DehydratedStmObject;
import org.codehaus.multiverse.multiversionedstm.MultiversionedStm;
import org.codehaus.multiverse.multiversionedstm.examples.IntegerValue;
import org.codehaus.multiverse.utils.iterators.InstanceIterator;
import static org.junit.Assert.fail;

import java.util.concurrent.TimeUnit;

public class WorkingOnBareHeapUpdateDriver extends JapexDriverBase {
    private MultiversionedStm stm;
    private long handle;
    private DefaultMultiversionedHeap heap;

    private long transactionCount = 100 * 15 * 1000 * 1000;
    private double transactionPerSec;

    @Override
    public void prepare(TestCase testCase) {
        readParams(testCase);
        heap = new DefaultMultiversionedHeap();
        stm = new MultiversionedStm(heap);
        setUpStructures();
    }

    private void setUpStructures() {
        handle = commit(stm, new IntegerValue());
    }

    private void readParams(TestCase testCase) {
        transactionCount = testCase.getLongParam("transactionCount");
    }

    @Override
    public void run(TestCase testCase) {
        long startNs = System.nanoTime();

        for (long k = 0; k < transactionCount; k++) {
            HeapSnapshot active = heap.getActiveSnapshot();
            IntegerValue value = (IntegerValue) ((DehydratedStmObject) active.read(handle)).___inflate(null);
            value.inc();
            CommitResult result = heap.commit(active, new InstanceIterator(value));
            if (!result.isSuccess())
                fail();
        }

        long endNs = System.nanoTime();
        transactionPerSec = (1.0 * TimeUnit.SECONDS.toNanos(1) * transactionCount) / (endNs - startNs);
        //System.out.printf("Performance is %s transactions/second\n", transactionPerSec);
    }


    @Override
    public void finish(TestCase testCase) {
        testCase.setParam(Constants.RESULT_UNIT, "transactions/second");
        testCase.setDoubleParam(Constants.RESULT_VALUE, transactionPerSec);
    }
}