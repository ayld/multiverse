package org.codehaus.multiverse.util.iterators;

import junit.framework.TestCase;

import static org.codehaus.multiverse.util.iterators.TestUtils.assertAsListContent;
import org.codehaus.multiverse.util.iterators.ArrayIterator;

public class ArrayIteratorTest extends TestCase {

    public void testEmptyArray() {
        assertAsListContent(new ArrayIterator(), new Object[]{});
    }

    public void testSingletonArray() {
        assertAsListContent(new ArrayIterator("foo"), new Object[]{"foo"});
    }

    public void testArray() {
        assertAsListContent(new ArrayIterator("1", "2", "3"), new Object[]{"1", "2", "3"});
    }

    public void testRemove() {
        try {
            new ArrayIterator().remove();
            fail();
        } catch (UnsupportedOperationException ex) {

        }
    }   
}
