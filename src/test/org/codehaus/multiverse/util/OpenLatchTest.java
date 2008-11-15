package org.codehaus.multiverse.util;

import junit.framework.TestCase;

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
