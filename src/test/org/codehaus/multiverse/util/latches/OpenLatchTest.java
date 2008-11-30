package org.codehaus.multiverse.util.latches;

import junit.framework.TestCase;
import org.codehaus.multiverse.util.latches.OpenLatch;

public class OpenLatchTest extends TestCase {
    private OpenLatch latch;

    @Override
    public void setUp(){
        latch = new OpenLatch();
    }

    public void testConstruction(){
        assertTrue(latch.isOpen());
    }

    public void testOpen(){
        latch.await();
        latch.open();
        assertTrue(latch.isOpen());
    }
}
