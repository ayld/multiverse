package org.codehaus.multiverse;

import static junit.framework.Assert.assertEquals;
import org.codehaus.multiverse.api.Stm;
import org.codehaus.multiverse.api.Transaction;
import org.codehaus.multiverse.utils.latches.Latch;
import static org.junit.Assert.fail;

import java.util.*;

/**
 * A Utility class containing various test support methods.
 *
 * @author Peter Veentjer.
 */
public class TestUtils {

    public static boolean equals(Object o1, Object o2) {
        if (o1 == null)
            return o2 == null;

        return o1.equals(o2);
    }

    public static long commit(Stm stm, Object item) {
        Transaction t = stm.startTransaction();
        long handle = t.attachAsRoot(item);
        t.commit();
        return handle;
    }

    public static void commitAll(Stm stm, Object... items) {
        Transaction t = stm.startTransaction();
        for (Object item : items)
            t.attachAsRoot(item);
        t.commit();
    }

    public static boolean randomBoolean() {
        return randomInteger(10) % 2 == 0;
    }

    public static int randomInteger(int max) {
        return (int) Math.round(Math.random() * max);
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
