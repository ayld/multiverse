package org.codehaus.multiverse;

import static junit.framework.Assert.assertEquals;

import java.util.*;

public class TestUtils {

    public static long randomLong(long i, int diff){
        return (long)(i + (diff * (Math.random()-0.5)));            
    }

    public static void startAll(Thread... threads){
        for(Thread thread: threads)
            thread.start();
    }

    public static void joinAll(Thread... threads){
        for (Thread thread : threads) {
            System.out.println("Joining " + thread.getName());
            try {
                thread.join();
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

    public static void sleepRandom(long maxMs) {
        if (maxMs == 0)
            return;

        sleep((long) (Math.random() * maxMs));
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
}
