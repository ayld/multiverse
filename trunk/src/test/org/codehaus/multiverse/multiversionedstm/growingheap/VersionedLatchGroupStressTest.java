package org.codehaus.multiverse.multiversionedstm.growingheap;

import static org.codehaus.multiverse.TestUtils.*;
import org.codehaus.multiverse.util.latches.Latch;
import org.codehaus.multiverse.util.latches.StandardLatch;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

public class VersionedLatchGroupStressTest {
    private static final int MAX_DELAY_MS = 0;

    private List<Latch> latches;
    private VersionedLatchGroup latchGroup;
    private long maxVersion = 100000;

    @Before
    public void setUp() {
        latchGroup = new VersionedLatchGroup(0);
        latches = new Vector(1000000);
    }

    public void assertAllLatchesAreOpened() {
        assertEquals(latches.size(), getLatchesOpenedCount());
    }

    public int getLatchesOpenedCount() {
        int result = 0;
        for (Latch latch : latches) {
            if (latch.isOpen())
                result++;
        }
        return result;
    }

    @Test
    public void test() {
        AddLatchThread[] addLatchThreads = createAddLatchThread(20);
        ActivateVersionThread[] activateVersionThreads = createActivateVersionThread(20);

        startAll(addLatchThreads);
        startAll(activateVersionThreads);
        joinAll(addLatchThreads);
        joinAll(activateVersionThreads);

        assertAllLatchesAreOpened();
    }

    public AddLatchThread[] createAddLatchThread(int count) {
        AddLatchThread[] result = new AddLatchThread[count];
        for (int k = 0; k < count; k++)
            result[k] = new AddLatchThread();
        return result;
    }

    public ActivateVersionThread[] createActivateVersionThread(int count) {
        ActivateVersionThread[] result = new ActivateVersionThread[count];
        for (int k = 0; k < count; k++)
            result[k] = new ActivateVersionThread();
        return result;
    }

    AtomicInteger threadCounter = new AtomicInteger();

    class AddLatchThread extends Thread {

        public AddLatchThread() {
            super("addLatchThread-" + threadCounter.incrementAndGet());
        }

        public void run() {
            long version;
            do {
                Latch latch = new StandardLatch();
                latches.add(latch);

                version = randomLong(latchGroup.getActiveVersion(), 10);
                if (version > maxVersion)
                    version = maxVersion;

                latchGroup.addLatch(version, latch);
                sleepRandom(MAX_DELAY_MS);
            } while (version < maxVersion);
        }
    }

    class ActivateVersionThread extends Thread {
        public ActivateVersionThread() {
            super("activateVersionThread-" + threadCounter.incrementAndGet());
        }

        public void run() {
            long version;
            do {
                version = randomLong(latchGroup.getActiveVersion(), 10);
                latchGroup.activateVersion(version);
                sleepRandom(MAX_DELAY_MS);
            } while (version < maxVersion);
        }
    }
}
