package org.multiverse;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertFalse;
import org.multiverse.utils.latches.Latch;
import org.multiverse.utils.InstrumentationProblemMonitor;
import org.multiverse.api.Transaction;
import org.multiverse.api.TransactionStatus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class TestUtils {

    public static void assertNoInstrumentationProblems(){
        assertFalse(InstrumentationProblemMonitor.INSTANCE.isSignalled());
    }

    public static void assertIsActive(Transaction t) {
        assertNotNull("No transaction found",t);
        assertEquals(TransactionStatus.active, t.getStatus());
    }

    public static void assertIsCommitted(Transaction t) {
        assertNotNull("No transaction found",t);
        assertEquals(TransactionStatus.committed, t.getStatus());
    }

    public static void assertIsAborted(Transaction t) {
        assertNotNull("No transaction found",t);
        assertEquals(TransactionStatus.aborted, t.getStatus());
    }

    public static boolean equals(Object o1, Object o2) {
        if (o1 == null) {
            return o2 == null;
        }

        return o1.equals(o2);
    }

    public static boolean randomBoolean() {
        return randomInt(10) % 2 == 0;
    }

    public static int randomInt(int max) {
        return (int) Math.round(Math.random() * max);
    }

    public static long randomLong(long i, int diff) {
        return (long) (i + (diff * (Math.random() - 0.5)));
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
        if (maxMs == 0) {
            return;
        }

        sleepMs((long) (Math.random() * maxMs));
        Thread.yield();
    }

    public static void sleepMs(long ms) {
        if (ms == 0) {
            return;
        }

        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            Thread.interrupted();
            throw new RuntimeException(ex);
        }
    }

    public static void startAll(TestThread... threads) {
        for (Thread thread : threads) {
            thread.start();
        }
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

    public static void assertIsOpen(Latch latch, boolean isOpen) {
        assertEquals(isOpen, latch.isOpen());
    }

    public static String readText(File errorOutputFile) {
        try {
            StringBuffer sb = new StringBuffer();
            BufferedReader reader = new BufferedReader(new FileReader(errorOutputFile));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            return sb.toString();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
