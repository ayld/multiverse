package org.multiverse.stms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.utils.clock.StrictClock;

/**
 * @author Peter Veentjer
 */
public class AbstractTransaction_commitTest {

    private StrictClock clock;

    @Before
    public void setUp() {
        clock = new StrictClock();
    }

    @Test
    public void commitOnStartedTransactionIsDelegated() {
        testIncomplete();
    }

    @Test
    public void commitOnStartedLeadsToAbortWhenExceptionIsThrow(){
        testIncomplete();
    }
    
    @Test
    public void commitOnCommittedTransactionIsIgnored() {
        Transaction t = new AbstractTransactionImpl(clock);
        t.commit();

        long version = clock.getTime();
        t.commit();
        assertIsCommitted(t);
        assertEquals(version, clock.getTime());
    }

    @Test
    public void commitOnAbortedTransactionFails() {
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
