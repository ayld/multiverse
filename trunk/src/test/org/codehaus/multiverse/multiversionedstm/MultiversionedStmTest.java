package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.api.exceptions.NoProgressPossibleException;
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
        MultiversionedStm.MultiversionedTransactionImpl t = stm.startTransaction();

        try {
            stm.startRetriedTransaction(t);
            fail();
        } catch (NoProgressPossibleException ex) {
        }
    }

    @Test
    public void testStartRetriedTransaction_TransactionBelongsToDifferentStm() throws InterruptedException {
        MultiversionedStm.MultiversionedTransactionImpl t = new MultiversionedStm().startTransaction();

        try {
            stm.startRetriedTransaction(t);
            fail();
        } catch (IllegalArgumentException ex) {
        }
    }
}
