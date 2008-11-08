package org.codehaus.multiverse.multiversionedstm;

import junit.framework.TestCase;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.codehaus.multiverse.multiversionedstm.examples.Person;
import static org.codehaus.multiverse.multiversionedstm.TestUtils.assertListContent;
import static org.codehaus.multiverse.multiversionedstm.TestUtils.assertSetContent;

public class HydratedCitizenIteratorTest extends TestCase {

    public void testEmpty() {
        Iterator it = new HydratedCitizenIterator(new Citizen[]{});
        assertHasNoNext(it);
        assertNextThrowsNoSuchElementException(it);
    }

    public void testSingleItemNoChildren() {
        Person p = new Person();
        Iterator it = new HydratedCitizenIterator(p);
        assertSetContent(it, p);
    }

    public void testMultipleItemsNoChildren() {
        Person p1 = new Person();
        Person p2 = new Person();
        Person p3 = new Person();
        Iterator it = new HydratedCitizenIterator(p1, p2, p3);
        assertSetContent(it, p1, p2, p3);
    }

    public void testItemWithChild() {
        Person p1 = new Person();
        Person p2 = new Person();
        p1.setParent(p2);

        Iterator it = new HydratedCitizenIterator(p1);
        assertSetContent(it, p1, p2);
    }

    public void testChain() {
        Person p1 = new Person();
        Person p2 = new Person();
        Person p3 = new Person();
        p2.setParent(p3);
        p1.setParent(p2);

        Iterator it = new HydratedCitizenIterator(p1);
        assertSetContent(it, p1, p2, p3);
    }

    public void testDuplicateItems() {
        Person p = new Person();
        Iterator it = new HydratedCitizenIterator(p, p);
        assertSetContent(it, p);
    }

    public void testDirectCycleItems() {
        Person p = new Person();
        p.setParent(p);
        Iterator it = new HydratedCitizenIterator(p);
        assertSetContent(it, p);
    }
    
    public void testIndirectCycleItems() {
        Person p1 = new Person();
        Person p2 = new Person();
        Person p3 = new Person();
        p1.setParent(p2);
        p2.setParent(p3);
        p3.setParent(p1);
        Iterator it = new HydratedCitizenIterator(p1);
        assertSetContent(it, p1, p2, p3);
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
