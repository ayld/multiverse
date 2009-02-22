package org.codehaus.multiverse;

import static junit.framework.Assert.assertEquals;
import org.codehaus.multiverse.core.Stm;
import org.codehaus.multiverse.core.Transaction;
import org.codehaus.multiverse.util.latches.Latch;
import static org.junit.Assert.fail;

import java.util.*;

/**
 * A Utility class containing various test support methods.
 *
 * @author Peter Veentjer.
 */
public class TestUtils {

    public static long atomicInsert(Stm stm, Object item) {
        Transaction t = stm.startTransaction();
        long handle = t.attachAsRoot(item);
        t.commit();
        return handle;
    }

    public static long randomLong(long i, int diff) {
        return (long) (i + (diff * (Math.random() - 0.5)));
    }

    public static void startAll(TestThread... threads) {
        for (Thread thread : threads)
            thread.start();
    }

    public static void joinAll(TestThread... threads) {
        for (TestThread thread : threads) {
            System.out.println("Joining " + thread.getName());
            try {
                thread.join();
                if (thread.getThrowable() != null) {
                    thread.getThrowable().printStackTrace();
                    fail();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
            System.out.println("Joined successfully " + thread.getName());
        }
    }

    public static <E> void assertAsListContent(Iterator<E> it, E... expectedItems) {
        List<E> expectedList = Arrays.asList(expectedItems);
        List<E> foundList = asList(it);
        assertEquals(expectedList, foundList);
    }

    public static <E> void assertAsSetContent(Iterator<E> it, E... expectedItems) {
        Set<E> expectedSet = new HashSet(Arrays.asList(expectedItems));
        Set<E> foundSet = new HashSet(asList(it));
        assertEquals(expectedSet, foundSet);
    }

    private static <E> List asList(Iterator<E> it) {
        List<E> result = new LinkedList<E>();
        for (; it.hasNext();)
            result.add(it.next());
        return result;
    }

    public static void sleepRandomMs(long maxMs) {
        if (maxMs == 0)
            return;

        sleep((long) (Math.random() * maxMs));
        Thread.yield();
    }

    public static void sleep(long ms) {
        if (ms == 0)
            return;

        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            Thread.interrupted();
            throw new RuntimeException(ex);
        }
    }

    public static void assertIsOpen(Latch latch, boolean isOpen) {
        assertEquals(isOpen, latch.isOpen());
    }
}
