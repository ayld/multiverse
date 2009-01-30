package org.codehaus.multiverse.util.iterators;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.fail;
import static org.codehaus.multiverse.TestUtils.assertAsListContent;
import org.junit.Test;

import java.util.NoSuchElementException;

public class CompositeIteratorTest {

    private CompositeIterator iterator;

    private void assertHasNoNext() {
        assertFalse(iterator.hasNext());

        try {
            iterator.next();
            fail();
        } catch (NoSuchElementException ex) {
        }
    }

    @Test
    public void testEmptyConstructor() {
        iterator = new CompositeIterator();
        assertHasNoNext();
    }

    @Test
    public void testWithEmptyIterator() {
        iterator = new CompositeIterator(EmptyIterator.INSTANCE);
        assertAsListContent(iterator);
    }

    @Test
    public void testSingleIterator() {
        Object item1 = "1";
        Object item2 = "2";
        Object item3 = "3";

        iterator = new CompositeIterator(new ArrayIterator(item1, item2, item3));
        assertAsListContent(iterator, item1, item2, item3);
    }

    @Test
    public void testMultipleIterators() {
        Object item1 = "1";
        Object item2 = "2";
        Object item3 = "3";
        Object item4 = "4";
        Object item5 = "5";

        iterator = new CompositeIterator(new ArrayIterator(item1, item2), new ArrayIterator(item3), new ArrayIterator(item4, item5));
        assertAsListContent(iterator, item1, item2, item3, item4, item5);
    }
}