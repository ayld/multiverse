package org.codehaus.multiverse.util;

import junit.framework.TestCase;

import static org.codehaus.multiverse.multiversionedstm.TestUtils.assertListContent;

public class ArrayIteratorTest extends TestCase {

    public void testEmptyArray() {
        assertListContent(new ArrayIterator(), new Object[]{});
    }

    public void testSingletonArray() {
        assertListContent(new ArrayIterator("foo"), new Object[]{"foo"});
    }

    public void testArray() {
        assertListContent(new ArrayIterator("1", "2", "3"), new Object[]{"1", "2", "3"});
    }

    public void testRemove() {
        try {
            new ArrayIterator().remove();
            fail();
        } catch (UnsupportedOperationException ex) {

        }
    }   
}
