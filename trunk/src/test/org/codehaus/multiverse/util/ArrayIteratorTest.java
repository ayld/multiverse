package org.codehaus.multiverse.util;

import junit.framework.TestCase;

import java.util.*;

import static org.codehaus.multiverse.multiversionedstm.TestUtils.assertContent;

public class ArrayIteratorTest extends TestCase {

    public void testEmptyArray() {
        assertContent(new ArrayIterator(), new Object[]{});
    }

    public void testSingletonArray() {
        assertContent(new ArrayIterator("foo"), new Object[]{"foo"});
    }

    public void testArray() {
        assertContent(new ArrayIterator("1", "2", "3"), new Object[]{"1", "2", "3"});
    }

    public void testRemove() {
        try {
            new ArrayIterator().remove();
            fail();
        } catch (UnsupportedOperationException ex) {

        }
    }   
}
