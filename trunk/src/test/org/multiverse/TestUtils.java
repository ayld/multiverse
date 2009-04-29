package org.multiverse;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.multiverse.api.Originator;
import org.multiverse.api.Stm;
import org.multiverse.api.Transaction;
import org.multiverse.api.TransactionState;
import org.multiverse.api.exceptions.NoCommittedDataFoundException;
import org.multiverse.examples.IntegerValue;
import org.multiverse.multiversionedstm.MultiversionedStm;
import org.multiverse.util.latches.Latch;

import java.util.*;

public class TestUtils {

    public static void assertIsActive(Transaction t) {
        assertEquals(TransactionState.active, t.getState());
    }

    public static void assertIsCommitted(Transaction t) {
        assertEquals(TransactionState.committed, t.getState());
    }

    public static void assertIsAborted(Transaction t) {
        assertEquals(TransactionState.aborted, t.getState());
    }

    public static boolean equals(Object o1, Object o2) {
        if (o1 == null)
            return o2 == null;

        return o1.equals(o2);
    }

    public static void assertWriteCount(MultiversionedStm stm, long expected) {
        assertEquals(expected, stm.getStatistics().getWriteCount());
    }

    public static void assertRematerializedCount(MultiversionedStm stm, long expected) {
        assertEquals(expected, stm.getStatistics().getMaterializedCount());
    }

    public static void assertGlobalVersion(MultiversionedStm stm, long expectedVersion) {
        assertEquals(expectedVersion, stm.getGlobalVersion());
    }

    public static void assertTransactionCommittedCount(MultiversionedStm stm, long commitCount) {
        assertEquals(commitCount, stm.getStatistics().getTransactionCommittedCount());
    }

    public static void assertTransactionReadonlyCount(MultiversionedStm stm, long readonlyCount) {
        assertEquals(readonlyCount, stm.getStatistics().getTransactionReadonlyCount());
    }

    public static void assertTransactionAbortedCount(MultiversionedStm stm, long abortedCount) {
        assertEquals(abortedCount, stm.getStatistics().getTransactionAbortedCount());
    }

    public static void assertTransactionRetriedCount(MultiversionedStm stm, long retriedCount) {
        assertEquals(retriedCount, stm.getStatistics().getTransactionRetriedCount());
    }

    public static void assertMaterializedCount(MultiversionedStm stm, long expectedMaterializedCount) {
        assertEquals(expectedMaterializedCount, stm.getStatistics().getMaterializedCount());
    }

    public static void assertIntegerValue(MultiversionedStm stm, Originator<IntegerValue> originator, int value) {
        Transaction t = stm.startTransaction();
        IntegerValue i = t.read(originator);
        assertEquals(value, i.get());
        t.commit();
    }

    public static void assertNoCommits(MultiversionedStm stm, Originator originator) {
        Transaction t = stm.startTransaction();
        try {
            t.read(originator);
            fail();
        } catch (NoCommittedDataFoundException ex) {
            assertTrue(true);
        } finally {
            t.abort();
        }
    }

    public static <T> Originator<T> commit(Stm stm, T item) {
        Transaction t = stm.startTransaction();
        Originator<T> originator = t.attach(item);
        t.commit();
        return originator;
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

    public static void assertIsOpen(Latch latch, boolean isOpen) {
        assertEquals(isOpen, latch.isOpen());
    }
}
