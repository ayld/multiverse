package org.codehaus.multiverse.multiversionedheap.listenersupport;

import static org.codehaus.multiverse.TestUtils.assertIsOpen;
import org.codehaus.multiverse.util.iterators.PLongArrayIterator;
import org.codehaus.multiverse.util.latches.CheapLatch;
import org.codehaus.multiverse.util.latches.Latch;
import org.codehaus.multiverse.util.latches.StandardLatch;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class DefaultListenerSupportTest {
    private DefaultListenerSupport listenerSupport;

    @Before
    public void setUp() {
        listenerSupport = new DefaultListenerSupport();
    }

    // ============== addListener ==================================

    @Test
    public void testFirstTimeListenerForVersion() {
        Latch latch = new CheapLatch();
        long handle = 1;
        long version = 9;
        listenerSupport.addListener(version, new PLongArrayIterator(handle), latch);

        assertFalse(latch.isOpen());

        listenerSupport.wakeupListeners(version + 1, new PLongArrayIterator(handle));

        assertTrue(latch.isOpen());
    }


    @Test
    public void testAddListener_emptyHandles() {
        //todo
    }

    @Test
    public void testAddListener_nullHandles() {
        Latch latch = new StandardLatch();
        try {
            listenerSupport.addListener(1, null, null);
            fail();
        } catch (NullPointerException ex) {
        }

        assertIsOpen(latch, false);
    }

    @Test
    public void testAddListener_nullLatch() {
        try {
            listenerSupport.addListener(1, new PLongArrayIterator(1), null);
            fail();
        } catch (NullPointerException ex) {

        }
    }

    // ============== wakeupListeners ==================================
}
