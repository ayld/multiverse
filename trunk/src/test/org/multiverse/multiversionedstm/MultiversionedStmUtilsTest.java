package org.multiverse.multiversionedstm;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import org.junit.Test;
import org.multiverse.api.Handle;
import org.multiverse.api.LazyReference;
import org.multiverse.multiversionedstm.examples.ExampleIntValue;
import org.multiverse.multiversionedstm.examples.ExamplePair;
import org.multiverse.util.ArrayIterator;
import org.multiverse.util.EmptyIterator;
import org.multiverse.util.InstanceIterator;

public class MultiversionedStmUtilsTest {

    @Test
    public void testEmptyIterator() {
        MaterializedObject first = MultiversionedStmUtils.initializeNextChain(EmptyIterator.INSTANCE);
        assertNull(first);
    }

    @Test
    public void testSingleElement() {
        MaterializedObject obj = new ExampleIntValue();
        MaterializedObject first = MultiversionedStmUtils.initializeNextChain(
                new InstanceIterator(new NonLazy(obj)));

        assertSame(obj, first);
        assertNull(obj.getNextInChain());
    }

    @Test
    public void testMultipleDirectElements() {
        MaterializedObject obj1 = new ExampleIntValue();
        MaterializedObject obj2 = new ExampleIntValue();
        MaterializedObject obj3 = new ExampleIntValue();

        MaterializedObject first = MultiversionedStmUtils.initializeNextChain(
                new ArrayIterator(new NonLazy(obj1), new NonLazy(obj2), new NonLazy(obj3)));

        assertSame(obj3, first);
        assertSame(obj2, obj3.getNextInChain());
        assertSame(obj1, obj3.getNextInChain().getNextInChain());
        assertNull(obj3.getNextInChain().getNextInChain().getNextInChain());
    }

    @Test
    public void testIndirect() {
        ExamplePair<ExampleIntValue, ExampleIntValue> pair = new ExamplePair(new ExampleIntValue(1), new ExampleIntValue(2));

        MaterializedObject first = MultiversionedStmUtils.initializeNextChain(
                new InstanceIterator(new NonLazy(pair)));

        assertSame(pair.getRight(), first);
        assertSame(pair.getLeft(), first.getNextInChain());
        assertSame(pair, first.getNextInChain().getNextInChain());
        assertNull(first.getNextInChain().getNextInChain().getNextInChain());
    }

    class NonLazy implements LazyReference {
        final MaterializedObject value;

        NonLazy(MaterializedObject value) {
            this.value = value;
        }

        @Override
        public boolean isLoaded() {
            return true;
        }

        @Override
        public Handle getHandle() {
            return value.getHandle();
        }

        @Override
        public Object get() {
            return value;
        }
    }
}
