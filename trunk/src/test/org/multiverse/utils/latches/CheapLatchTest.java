package org.multiverse.utils.latches;

import static org.junit.Assert.*;
import org.junit.Test;
import org.multiverse.TestThread;
import static org.multiverse.TestUtils.joinAll;
import static org.multiverse.TestUtils.sleepMs;

import java.util.concurrent.TimeUnit;

/**
 * @author Peter Veentjer
 */
public class CheapLatchTest {

    @Test
    public void openAlreadyOpenLatch() {
        Latch latch = new CheapLatch(true);

        latch.open();
        assertTrue(latch.isOpen());
    }

    @Test
    public void tryAwaitIsUnsupported() throws InterruptedException {
        Latch latch = new CheapLatch();

        try {
            latch.tryAwait(10, TimeUnit.MILLISECONDS);
            fail();
        } catch (UnsupportedOperationException ex) {
        }

        assertFalse(latch.isOpen());
    }

    @Test
    public void awaitOnOpenLatch() {
        Latch latch = new CheapLatch(true);
        AwaitThread thread1 = new AwaitThread(latch);
        thread1.start();

        joinAll(thread1);
        assertFalse(thread1.waiting);
        assertTrue(latch.isOpen());
    }

    @Test
    public void awaitOnClosedLatch() {
        Latch latch = new CheapLatch(false);
        AwaitThread thread1 = new AwaitThread(latch);
        AwaitThread thread2 = new AwaitThread(latch);
        thread1.start();
        thread2.start();

        sleepMs(500);

        assertTrue(thread1.waiting);
        assertTrue(thread2.waiting);
        latch.open();

        joinAll(thread1, thread2);
        assertFalse(thread1.waiting);
        assertFalse(thread2.waiting);
        assertTrue(latch.isOpen());
    }


    class AwaitThread extends TestThread {
        private final Latch latch;
        volatile boolean waiting;

        AwaitThread(Latch latch) {
            this.latch = latch;
        }

        @Override
        public void run() {
            waiting = true;
            try {
                try {

                    latch.await();
                } catch (InterruptedException ex) {
                    fail();
                }
            } finally {
                waiting = false;
            }
        }
    }
}
