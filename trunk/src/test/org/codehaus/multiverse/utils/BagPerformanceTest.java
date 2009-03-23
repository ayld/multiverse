package org.codehaus.multiverse.utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

/**
 * A performance test for {@link org.codehaus.multiverse.utils.Bag} to make sure that it performs better than the structure
 * it replaced: java.util.LinkedList.
 *
 * @author Peter Veentjer.
 */
public class BagPerformanceTest {

    public Object item = "foo";
    private int repeatCount = 15000;
    private long startNs;

    @Before
    public void setUp() {
        startNs = System.nanoTime();
    }

    @After
    public void tearDown() {
        long endNs = System.nanoTime();
        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(endNs - startNs);
        System.out.printf("test took %s ms\n", elapsedMs);
    }

    @Test
    public void testBag() {
        Bag bag = new Bag();
        for (int k = 0; k < repeatCount; k++) {
            for (int l = 0; l < k; l++)
                bag.add(item);

            while (!bag.isEmpty())
                bag.takeAny();
        }
    }

    @Test
    public void testList() {
        LinkedList list = new LinkedList();
        for (int k = 0; k < repeatCount; k++) {
            for (int l = 0; l < k; l++)
                list.add(item);

            while (!list.isEmpty())
                list.removeFirst();
        }
    }

    //@Test
    //test is disabled because the arraylist has terrible performance for insert en deletes.
    public void testArrayList() {
        ArrayList list = new ArrayList();
        for (int k = 0; k < repeatCount; k++) {
            for (int l = 0; l < k; l++)
                list.add(item);

            while (!list.isEmpty())
                list.remove(0);
        }
    }
}
