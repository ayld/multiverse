package org.codehaus.multiverse.multiversionedstm.growingheap;

import junit.framework.TestCase;
import static org.codehaus.multiverse.TestUtils.assertIsOpen;
import org.codehaus.multiverse.util.latches.Latch;
import org.codehaus.multiverse.util.latches.StandardLatch;

public class DefaultListenerSupportTest extends TestCase {
    private DefaultListenerSupport listenerSupport;

    public void setUp() {
        listenerSupport = new DefaultListenerSupport();
    }

    // ============== addListener ==================================

    public void testAddListener(){

    }

    public void testAddListener_emptyHandles(){
        //todo
    }

    public void testAddListener_nullHandles() {
        Latch latch = new StandardLatch();
        try {
            listenerSupport.addListener(1, null, null);
            fail();
        } catch (NullPointerException ex) {
        }

        assertIsOpen(latch, false);
    }

    public void testAddListener_nullLatch() {
        try {
            listenerSupport.addListener(1, new long[]{1}, null);
            fail();
        } catch (NullPointerException ex) {

        }
    }

    // ============== wakeupListeners ==================================
}
