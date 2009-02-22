package org.codehaus.multiverse.multiversionedstm.performance;

import com.sun.japex.Constants;
import com.sun.japex.JapexDriverBase;
import com.sun.japex.TestCase;
import static org.codehaus.multiverse.TestUtils.atomicInsert;
import org.codehaus.multiverse.multiversionedstm.MultiversionedHeap.CommitResult;
import org.codehaus.multiverse.multiversionedstm.MultiversionedHeapSnapshot;
import org.codehaus.multiverse.multiversionedstm.MultiversionedStm;
import org.codehaus.multiverse.multiversionedstm.examples.IntegerValue;
import org.codehaus.multiverse.multiversionedstm.growingheap.SmartGrowingMultiversionedHeap;
import org.codehaus.multiverse.util.iterators.InstanceIterator;
import static org.junit.Assert.fail;

import java.util.concurrent.TimeUnit;

public class WorkingOnBareHeapUpdateDriver extends JapexDriverBase {
    private MultiversionedStm stm;
    private long handle;
    private SmartGrowingMultiversionedHeap heap;

    private long transactionCount = 100 * 15 * 1000 * 1000;
    private double transactionPerSec;

    @Override
    public void prepare(TestCase testCase) {
        readParams(testCase);
        heap = new SmartGrowingMultiversionedHeap();
        stm = new MultiversionedStm(heap);
        setUpStructures();
    }

    private void setUpStructures() {
        handle = atomicInsert(stm, new IntegerValue());
    }

    private void readParams(TestCase testCase) {
        transactionCount = testCase.getLongParam("transactionCount");
    }

    @Override
    public void run(TestCase testCase) {
        long startNs = System.nanoTime();

        for (long k = 0; k < transactionCount; k++) {
            MultiversionedHeapSnapshot active = heap.getActiveSnapshot();
            IntegerValue value = (IntegerValue) active.read(handle).hydrate(null);
            value.inc();
            CommitResult result = heap.commit(active, new InstanceIterator(value.___dehydrate()));
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