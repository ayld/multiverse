package org.codehaus.multiverse.multiversionedstm.utils;

import static org.codehaus.multiverse.TestUtils.assertIsOpen;
import org.codehaus.multiverse.util.latches.Latch;
import org.codehaus.multiverse.util.latches.StandardLatch;
import static org.junit.Assert.fail;
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
    public void testAddListener() {
        //todo
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
            listenerSupport.addListener(1, new long[]{1}, null);
            fail();
        } catch (NullPointerException ex) {

        }
    }

    // ============== wakeupListeners ==================================
}
