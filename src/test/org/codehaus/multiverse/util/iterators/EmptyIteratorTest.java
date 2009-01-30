package org.codehaus.multiverse.util.iterators;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.fail;
import org.junit.Before;
import org.junit.Test;

import java.util.NoSuchElementException;


public class EmptyIteratorTest {
    private EmptyIterator it;

    @Before
    public void setUp() {
        it = EmptyIterator.INSTANCE;
    }

    @Test
    public void testHasNext() {
        assertFalse(it.hasNext());
    }

    @Test
    public void testNext() {
        try {
            it.next();
            fail();
        } catch (NoSuchElementException ex) {
        }
    }

    @Test
    public void testRemove() {
        try {
            it.remove();
            fail();
        } catch (UnsupportedOperationException ex) {
        }
    }
}
