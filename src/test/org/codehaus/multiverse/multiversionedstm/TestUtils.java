package org.codehaus.multiverse.multiversionedstm;

import static junit.framework.Assert.assertEquals;

import java.util.Iterator;
import java.util.List;
import java.util.Arrays;
import java.util.LinkedList;

public class TestUtils {

    public static <E> void assertContent(Iterator<E> it, E... expectedItems) {
        List<E> expectedList = Arrays.asList(expectedItems);
        List<E> foundList = asList(it);
        assertEquals(expectedList, foundList);
    }

    private static <E> List asList(Iterator<E> it) {
        List<E> result = new LinkedList<E>();
        for (; it.hasNext();)
            result.add(it.next());
        return result;
    }
}
