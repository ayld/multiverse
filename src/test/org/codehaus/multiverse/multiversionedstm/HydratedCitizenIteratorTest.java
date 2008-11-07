package org.codehaus.multiverse.multiversionedstm;

import junit.framework.TestCase;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.codehaus.multiverse.multiversionedstm.examples.Person;
import static org.codehaus.multiverse.multiversionedstm.TestUtils.assertContent;

public class HydratedCitizenIteratorTest extends TestCase {

    public void testEmpty() {
        Iterator it = new HydratedCitizenIterator();
        assertHasNoNext(it);
        assertNextThrowsNoSuchElementException(it);
    }

    public void testSingleItemNoChildren() {
        Person p = new Person();
        Iterator it = new HydratedCitizenIterator(p);
        assertContent(it, p);
    }

    public void testMultipleItemsNoChildren() {
        Person p1 = new Person();
        Person p2 = new Person();
        Person p3 = new Person();
        Iterator it = new HydratedCitizenIterator(p1, p2, p3);
        assertContent(it, p1, p2, p3);
    }

    public void testItemWithChild() {
        Person p1 = new Person();
        Person p2 = new Person();
        p1.setParent(p2);

        Iterator it = new HydratedCitizenIterator(p1);
        assertContent(it, p1, p2);
    }

    public void testDuplicateItems() {
        Person p = new Person();
        Iterator it = new HydratedCitizenIterator(p, p);
        assertContent(it, p);
    }

    public void testDirectCycleItems() {
        Person p = new Person();
        p.setParent(p);
        Iterator it = new HydratedCitizenIterator(p, p);
        assertContent(it, p);
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
