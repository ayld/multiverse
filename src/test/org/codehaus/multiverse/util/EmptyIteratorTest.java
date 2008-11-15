package org.codehaus.multiverse.util;

import junit.framework.TestCase;

public class EmptyIteratorTest extends TestCase {
    private EmptyIterator it;

    public void setUp(){
        it = EmptyIterator.INSTANCE;
    }

    public void testHasNext(){
        assertFalse(it.hasNext());
    }

    public void testNext(){
        try{
            it.next();
            fail();
        }catch(UnsupportedOperationException ex){
        }
    }

    public void testRemove(){
         try{
            it.remove();
            fail();
        }catch(UnsupportedOperationException ex){
        }
    }
}
