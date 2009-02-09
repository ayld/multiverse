package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.core.NoProgressPossibleException;
import org.junit.Test;

public class MultiversionedStmTest extends AbstractMultiversionedStmTest {

    @Test
    public void testStartRetriedTransaction_nullArgument() throws InterruptedException {
        try {
            stm.startRetriedTransaction(null);
            fail();
        } catch (NullPointerException ex) {
        }
    }

    @Test
    public void testStartRetriedTransaction_noProgressPossible() throws InterruptedException {
        MultiversionedStm.MultiversionedTransaction t = stm.startTransaction();

        try {
            stm.startRetriedTransaction(t);
            fail();
        } catch (NoProgressPossibleException ex) {
        }
    }

    @Test
    public void testStartRetriedTransaction_TransactionBelongsToDifferentStm() throws InterruptedException {
        MultiversionedStm.MultiversionedTransaction t = new MultiversionedStm().startTransaction();

        try {
            stm.startRetriedTransaction(t);
            fail();
        } catch (IllegalArgumentException ex) {
        }
    }
}
