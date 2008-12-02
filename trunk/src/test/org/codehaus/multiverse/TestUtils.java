package org.codehaus.multiverse;

import static junit.framework.Assert.assertEquals;

import java.util.*;

public class TestUtils {

    public static void joinAll(Thread... threads) throws InterruptedException {
        for (Thread thread : threads) {
            System.out.println("Joining " + thread.getName());
            thread.join();
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

    public static void sleepRandom(long maxMs) {
        if (maxMs == 0)
            return;

        sleep((long) (Math.random() * maxMs));
    }

    public static void sleep(long maxMs) {
        if (maxMs == 0)
            return;

        try {
            Thread.sleep(maxMs);
        } catch (InterruptedException ex) {
            Thread.interrupted();
            throw new RuntimeException(ex);
        }
    }
}
