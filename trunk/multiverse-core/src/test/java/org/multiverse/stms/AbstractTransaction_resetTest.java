package org.multiverse.stms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.ResetFailureException;
import org.multiverse.utils.clock.StrictClock;

/**
 * @author Peter Veentjer
 */
public class AbstractTransaction_resetTest {

    private StrictClock clock;

    @Before
    public void setUp() {
        clock = new StrictClock();
    }

    @Test
    public void resetOnStartedTransaction() {
        testIncomplete();
    }

    @Test
    public void test() {
        testIncomplete();
    }

    @Test
    public void resetOnActiveTransactionFails() {
        Transaction t = new AbstractTransactionImpl(clock);
        long version = clock.getTime();

        try {
            t.restart();
            fail();
        } catch (ResetFailureException expected) {

        }
        assertIsActive(t);
        assertEquals(version, clock.getTime());
    }

    @Test
    public void resetOnCommittedTransactionSucceeds() {
        Transaction t = new AbstractTransactionImpl(clock);
        t.commit();

        long version = clock.getTime();
        t.commit();
        assertIsCommitted(t);
        assertEquals(version, clock.getTime());
    }

    @Test
    public void resetOnAbortedTransactionSucceeds() {
        Transaction t = new AbstractTransactionImpl(clock);
        t.abort();

        long version = clock.getTime();
        try {
            t.commit();
            fail();
        } catch (DeadTransactionException ex) {

        }

        assertIsAborted(t);
        assertEquals(version, clock.getTime());
    }
}
