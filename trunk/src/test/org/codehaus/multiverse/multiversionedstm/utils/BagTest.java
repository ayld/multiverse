package org.codehaus.multiverse.multiversionedstm.utils;

import org.codehaus.multiverse.util.Bag;
import static org.junit.Assert.*;
import org.junit.Test;

public class BagTest {

    @Test(expected = IllegalStateException.class)
    public void takeAnyOfEmptyBagFails() {
        Bag bag = new Bag();
        bag.takeAny();
    }

    @Test(expected = NullPointerException.class)
    public void addOfNullFails() {
        Bag bag = new Bag();
        bag.add(null);
    }

    @Test
    public void duplicatesAreNotRemoved() {
        Bag bag = new Bag();
        String item = "foo";

        bag.add(item);
        bag.add(item);

        assertSame(item, bag.takeAny());
        assertSame(item, bag.takeAny());
        assertTrue(bag.isEmpty());
    }

    @Test
    public void normalUsage() {
        Bag bag = new Bag();
        assertTrue(bag.isEmpty());

        String item1 = "foo";
        String item2 = "bar";

        bag.add(item1);
        assertFalse(bag.isEmpty());

        bag.add(item2);
        assertFalse(bag.isEmpty());

        assertSame(item2, bag.takeAny());
        assertFalse(bag.isEmpty());

        assertSame(item1, bag.takeAny());
        assertTrue(bag.isEmpty());
    }
}
