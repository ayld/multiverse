package org.codehaus.multiverse.api;

import org.codehaus.multiverse.api.exceptions.NoProgressPossibleException;
import org.codehaus.multiverse.multiversionedheap.HeapSnapshot;
import org.codehaus.multiverse.multiversionedstm.MultiversionedStm;
import static org.codehaus.multiverse.multiversionedstm.TransactionMethods.retry;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import org.junit.Test;

public class TransactionTemplateTest {

    @Test
    public void testWriteConflict() {
        //todo
    }

    @Test
    public void testRetry() {
        //todo
    }

    @Test
    public void testException() {
        //todo
    }

    @Test
    public void testRuntimeException() {
        //todo
    }

    public void testInterruptedException() {
        //todo
    }

    @Test
    public void testStmException() {
        //todo
    }

    @Test
    public void testSuccess() {
        //todo
    }

    @Test
    public void testNoConditionVariablesAndRetryShouldNotResultInInfinitiveLoop() {
        MultiversionedStm stm = new MultiversionedStm();
        HeapSnapshot oldSnapshot = stm.getHeap().getActiveSnapshot();

        try {
            new TransactionTemplate(stm) {
                @Override
                protected Object execute(Transaction t) {
                    retry();
                    return null;
                }
            }.execute();

            fail();
        } catch (NoProgressPossibleException ex) {
            assertSame(oldSnapshot, stm.getHeap().getActiveSnapshot());
        }
    }
}
