#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )


import static org.junit.Assert.*;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.utils.ThreadLocalTransaction.setThreadLocalTransaction;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Stm;
import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.RetryError;

/**
 * Unit tests for the {@link CubbyHole}.
 * <p>
 * <strong>Note:</strong> The tests require the Multiverse agent in order to run correctly
 * outside Surefire, e.g. in Eclipse. See the configuration of the {@code maven-surefire-plugin}
 * for the correct VM argument to set.
 * 
 * @author Andrew Phillips
 * @see org.multiverse.datastructures.refs.RefTest
 */
public class CubbyHoleTest {
    private Stm stm;

    @Before
    public void setUp() {
        stm = getGlobalStmInstance();
        setThreadLocalTransaction(null);
    }

    @After
    public void tearDown() {
        setThreadLocalTransaction(null);
    }

    // ============== rollback =================

    @Test
    public void rollback() {
        rollback(null, null);
        rollback(null, "foo");
        rollback("bar", "foo");
        rollback("bar", null);
    }

    private void rollback(String initialContents, String newContents) {
        CubbyHole<String> cubbyHole = new CubbyHole<String>(initialContents);

        long version = stm.getClockVersion();

        Transaction t = stm.startUpdateTransaction("rollback");
        setThreadLocalTransaction(t);
        cubbyHole.set(newContents);
        t.abort();

        assertEquals(version, stm.getClockVersion());
        assertSame(initialContents, cubbyHole.get());
    }

    // ========== constructors ==================

    @Test
    public void noArgConstruction() {
        long version = stm.getClockVersion();

        CubbyHole<String> cubbyHole = new CubbyHole<String>();

        assertEquals(version + 1, stm.getClockVersion());
        assertNull(cubbyHole.get());
    }

    @Test
    public void nullConstruction() {
        long version = stm.getClockVersion();

        CubbyHole<String> cubbyHole = new CubbyHole<String>();

        assertEquals(version + 1, stm.getClockVersion());
        assertEquals(null, cubbyHole.get());
    }

    @Test
    public void nonNullConstruction() {
        long version = stm.getClockVersion();
        String s = "foo";
        CubbyHole<String> cubbyHole = new CubbyHole<String>(s);

        assertEquals(version + 1, stm.getClockVersion());
        assertEquals(s, cubbyHole.get());
    }

    @Test
    public void testIsEmpty() {
        CubbyHole<String> cubbyHole = new CubbyHole<String>();

        long version = stm.getClockVersion();
        assertTrue(cubbyHole.isEmpty());
        assertEquals(version, stm.getClockVersion());

        cubbyHole.set("foo");

        version = stm.getClockVersion();
        assertFalse(cubbyHole.isEmpty());
        assertEquals(version, stm.getClockVersion());
    }

    @Test
    public void testSetChangedReferenced() {
        String oldContents = "foo";

        CubbyHole<String> cubbyHole = new CubbyHole<String>(oldContents);

        long version = stm.getClockVersion();

        String newContents = "bar";
        cubbyHole.set(newContents);
        assertEquals(version + 1, stm.getClockVersion());
        assertSame(newContents, cubbyHole.get());
    }

    @Test
    public void testSetUnchangedReferences() {
        String oldContents = "foo";

        CubbyHole<String> cubbyHole = new CubbyHole<String>(oldContents);

        long version = stm.getClockVersion();

        cubbyHole.set(oldContents);
        assertEquals(version, stm.getClockVersion());
        assertSame(oldContents, cubbyHole.get());
    }

    @Test
    public void getOrAwaitCompletesIfRefNotNull() {
        String oldContents = "foo";

        CubbyHole<String> cubbyHole = new CubbyHole<String>(oldContents);

        long version = stm.getClockVersion();

        String result = cubbyHole.getOrAwait();
        assertEquals(version, stm.getClockVersion());
        assertSame(oldContents, result);
    }

    @Test
    public void getOrAwaitRetriesIfNull() {
        CubbyHole<String> cubbyHole = new CubbyHole<String>();

        long version = stm.getClockVersion();

        //we start a transaction because we don't want to lift on the retry mechanism
        //of the transaction that else would be started on the getOrAwait method.
        Transaction t = stm.startUpdateTransaction(null);
        setThreadLocalTransaction(t);
        try {
            cubbyHole.getOrAwait();
            fail();
        } catch (RetryError retryError) { 
            // expected
        }
        assertEquals(version, stm.getClockVersion());
    }
}