package org.codehaus.multiverse.util.iterators;

import junit.framework.TestCase;

import java.util.NoSuchElementException;

import static org.codehaus.multiverse.TestUtils.assertAsListContent;

public class CompositeIteratorTest extends TestCase {
    private CompositeIterator iterator;

    private void assertHasNoNext(){
        assertFalse(iterator.hasNext());

        try{
            iterator.next();
            fail();
        }catch(NoSuchElementException ex){
        }
    }

    public void testEmptyConstructor() {
        iterator = new CompositeIterator();
        assertHasNoNext();
    }

    public void testWithEmptyIterator(){
        iterator = new CompositeIterator(EmptyIterator.INSTANCE);
        assertAsListContent(iterator);
    }

    public void testSingleIterator(){
        Object item1 = "1";
        Object item2 = "2";
        Object item3 = "3";

        iterator = new CompositeIterator(new ArrayIterator(item1,item2,item3));
        assertAsListContent(iterator, item1, item2, item3);
    }

    public void testMultipleIterators(){
        Object item1 = "1";
        Object item2 = "2";
        Object item3 = "3";
        Object item4 = "4";
        Object item5 = "5";

        iterator = new CompositeIterator(new ArrayIterator(item1,item2), new ArrayIterator(item3), new ArrayIterator(item4, item5));
        assertAsListContent(iterator, item1, item2, item3, item4, item5);
    }
}