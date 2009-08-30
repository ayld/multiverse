package org.multiverse.datastructures.refs.manual;

import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Stm;
import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.RetryError;
import org.multiverse.datastructures.refs.Ref;
import org.multiverse.utils.GlobalStmInstance;
import static org.multiverse.utils.TransactionThreadLocal.setThreadLocalTransaction;

public class RefTest {
    private Stm stm;

    @Before
    public void setUp() {
        stm = GlobalStmInstance.get();
        setThreadLocalTransaction(null);
    }

    @After
    public void tearDown() {
        setThreadLocalTransaction(null);
    }

    @Test
    public void noArgConstruction() {
        long version = stm.getClockVersion();

        org.multiverse.datastructures.refs.Ref<String> ref = new Ref<String>();

        assertEquals(version + 1, stm.getClockVersion());
        assertNull(ref.get());
    }

    @Test
    public void nullConstruction() {
        long version = stm.getClockVersion();

        Ref<String> ref = new Ref<String>();

        assertEquals(version + 1, stm.getClockVersion());
        assertEquals(null, ref.get());
    }

    @Test
    public void nonNullConstruction() {
        long version = stm.getClockVersion();
        String s = "foo";
        Ref<String> ref = new Ref<String>(s);

        assertEquals(version + 1, stm.getClockVersion());
        assertEquals(s, ref.get());
    }

    @Test
    public void testIsNull() {
        Ref<String> ref = new Ref<String>();

        long version = stm.getClockVersion();
        assertTrue(ref.isNull());
        assertEquals(version, stm.getClockVersion());

        ref.set("foo");

        version = stm.getClockVersion();
        assertFalse(ref.isNull());
        assertEquals(version, stm.getClockVersion());
    }

    @Test
    public void testSetFromNullToNull() {
        Ref<String> ref = new Ref<String>();

        long version = stm.getClockVersion();
        String result = ref.set(null);
        assertNull(result);
        assertEquals(version, stm.getClockVersion());
        assertNull(ref.get());
    }

    @Test
    public void testSetFromNullToNonNull() {
        Ref<String> ref = new Ref<String>();

        long version = stm.getClockVersion();
        String newRef = "foo";
        String result = ref.set(newRef);
        assertNull(result);
        assertEquals(version + 1, stm.getClockVersion());
        assertSame(newRef, ref.get());
    }

    @Test
    public void testSetFromNonNullToNull() {
        String oldRef = "foo";
        Ref<String> ref = new Ref<String>(oldRef);

        long version = stm.getClockVersion();

        String result = ref.set(null);
        assertSame(oldRef, result);
        assertEquals(version + 1, stm.getClockVersion());
        assertNull(ref.get());
    }

    @Test
    public void testSetChangedReferenced() {
        String oldRef = "foo";

        Ref<String> ref = new Ref<String>(oldRef);

        long version = stm.getClockVersion();

        String newRef = "bar";
        String result = ref.set(newRef);
        assertSame(oldRef, result);
        assertEquals(version + 1, stm.getClockVersion());
        assertSame(newRef, ref.get());
    }

    @Test
    public void testSetUnchangedReferences() {
        String oldRef = "foo";

        Ref<String> ref = new Ref<String>(oldRef);

        long version = stm.getClockVersion();

        String result = ref.set(oldRef);
        assertSame(oldRef, result);
        assertEquals(version, stm.getClockVersion());
        assertSame(oldRef, ref.get());
    }

    @Test
    public void testSetEqualIsNotUsedButReferenceEquality() {
        String oldRef = new String("foo");

        Ref<String> ref = new Ref<String>(oldRef);

        long version = stm.getClockVersion();

        String newRef = new String("foo");
        String result = ref.set(newRef);
        assertSame(oldRef, result);
        assertEquals(version + 1, stm.getClockVersion());
        assertSame(newRef, ref.get());
    }

    @Test
    public void testSetAndUnsetIsNotSeenAsChange() {
        String oldRef = "foo";
        Ref<String> ref = new Ref<String>(oldRef);

        long version = stm.getClockVersion();
        Transaction t = stm.startUpdateTransaction(null);
        setThreadLocalTransaction(t);
        String newRef = "bar";
        ref.set(newRef);
        ref.set(oldRef);
        t.commit();
        setThreadLocalTransaction(null);

        assertEquals(version, stm.getClockVersion());
        assertSame(oldRef, ref.get());
    }

    @Test
    public void getOrAwaitCompletesIfRefNotNull() {
        String oldRef = "foo";

        Ref<String> ref = new Ref<String>(oldRef);

        long version = stm.getClockVersion();

        String result = ref.getOrAwait();
        assertEquals(version, stm.getClockVersion());
        assertSame(oldRef, result);
    }

    @Test
    public void getOrAwaitRetriesIfNull() {
        Ref<String> ref = new Ref<String>();

        long version = stm.getClockVersion();

        //we start a transaction because we don't want to lift on the retry mechanism
        //of the transaction that else would be started on the getOrAwait method.
        Transaction t = stm.startUpdateTransaction(null);
        setThreadLocalTransaction(t);
        try {
            ref.getOrAwait();
            fail();
        } catch (RetryError retryError) {

        }
        assertEquals(version, stm.getClockVersion());
    }
}