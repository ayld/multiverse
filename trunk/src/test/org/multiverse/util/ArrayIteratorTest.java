package org.multiverse.util;

import static org.junit.Assert.fail;
import org.junit.Test;
import static org.multiverse.TestUtils.assertAsListContent;
import org.multiverse.util.ArrayIterator;

public class ArrayIteratorTest {

    @Test
    public void testEmptyArray() {
        assertAsListContent(new ArrayIterator(), new Object[]{});
    }

    @Test
    public void testSingletonArray() {
        assertAsListContent(new ArrayIterator("foo"), new Object[]{"foo"});
    }

    @Test
    public void testArray() {
        assertAsListContent(new ArrayIterator("1", "2", "3"), new Object[]{"1", "2", "3"});
    }

    @Test
    public void testRemove() {
        try {
            new ArrayIterator().remove();
            fail();
        } catch (UnsupportedOperationException ex) {

        }
    }
}
