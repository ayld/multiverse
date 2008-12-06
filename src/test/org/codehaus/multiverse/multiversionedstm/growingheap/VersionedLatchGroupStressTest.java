package org.codehaus.multiverse.multiversionedstm.growingheap;

import junit.framework.TestCase;
import static org.codehaus.multiverse.TestUtils.*;
import org.codehaus.multiverse.util.latches.Latch;
import org.codehaus.multiverse.util.latches.StandardLatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Create a number if listen latches and register them by multiple threads.
 * Create a number of version increasing threads.
 * Let the test run from beginVersion to endVersion
 * check that all the latches that are registered including endVersion have been openen
 */
public class VersionedLatchGroupStressTest extends TestCase {
    private List<Latch> latches;
    private VersionedLatchGroup latchGroup;
    private long maxVersion = 1000000;

    @Override
    public void setUp() {
        latchGroup = new VersionedLatchGroup(0);
        latches = new ArrayList(1000000);
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

    public void test() {
        AddLatchThread[] addLatchThreads = createAddLatchThread(20);
        ActivateVersionThread[] activateVersionThreads = createActivateVersionThread(20);

        startAll(addLatchThreads);
        startAll(activateVersionThreads);
        joinAll(addLatchThreads);
        joinAll(activateVersionThreads);

        assertAllLatchesAreOpened();
    }

    public AddLatchThread[] createAddLatchThread(int count){
        AddLatchThread[] result = new AddLatchThread[count];
        for(int k=0;k<count;k++)
            result[k]=new AddLatchThread();
        return result;
    }

    public ActivateVersionThread[] createActivateVersionThread(int count){
        ActivateVersionThread[] result = new ActivateVersionThread[count];
        for(int k=0;k<count;k++)
            result[k]=new ActivateVersionThread();
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
                sleepRandom(5);
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
                sleepRandom(5);
            } while (version < maxVersion);
        }
    }
}
