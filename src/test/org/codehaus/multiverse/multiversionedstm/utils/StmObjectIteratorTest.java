package org.codehaus.multiverse.multiversionedstm.utils;

import junit.framework.TestCase;
import static org.codehaus.multiverse.TestUtils.assertAsSetContent;
import org.codehaus.multiverse.multiversionedstm.StmObject;
import org.codehaus.multiverse.multiversionedstm.examples.Person;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class StmObjectIteratorTest extends TestCase {

    public void testEmpty() {
        Iterator it = new StmObjectIterator(new StmObject[]{});
        assertHasNoNext(it);
        assertNextThrowsNoSuchElementException(it);
    }

    public void testSingleItemNoChildren() {
        Person p = new Person();
        Iterator it = new StmObjectIterator(p);
        assertAsSetContent(it, p);
    }

    public void testMultipleItemsNoChildren() {
        Person p1 = new Person();
        Person p2 = new Person();
        Person p3 = new Person();
        Iterator it = new StmObjectIterator(p1, p2, p3);
        assertAsSetContent(it, p1, p2, p3);
    }

    public void testItemWithChild() {
        Person p1 = new Person();
        Person p2 = new Person();
        p1.setParent(p2);

        Iterator it = new StmObjectIterator(p1);
        assertAsSetContent(it, p1, p2);
    }

    public void testChain() {
        Person p1 = new Person();
        Person p2 = new Person();
        Person p3 = new Person();
        p2.setParent(p3);
        p1.setParent(p2);

        Iterator it = new StmObjectIterator(p1);
        assertAsSetContent(it, p1, p2, p3);
    }

    public void testDuplicateItems() {
        Person p = new Person();
        Iterator it = new StmObjectIterator(p, p);
        assertAsSetContent(it, p);
    }

    public void testDirectCycleItems() {
        Person p = new Person();
        p.setParent(p);
        Iterator it = new StmObjectIterator(p);
        assertAsSetContent(it, p);
    }

    public void testIndirectCycleItems() {
        Person p1 = new Person();
        Person p2 = new Person();
        Person p3 = new Person();
        p1.setParent(p2);
        p2.setParent(p3);
        p3.setParent(p1);
        Iterator it = new StmObjectIterator(p1);
        assertAsSetContent(it, p1, p2, p3);
    }

    public void assertHasNext(Iterator it) {
        assertTrue(it.hasNext());
    }

    public void assertHasNoNext(Iterator it) {
        assertFalse(it.hasNext());
    }

    public void assertNextThrowsNoSuchElementException(Iterator it) {
        try {
            it.next();
            fail();
        } catch (NoSuchElementException ex) {

        }
    }
}
