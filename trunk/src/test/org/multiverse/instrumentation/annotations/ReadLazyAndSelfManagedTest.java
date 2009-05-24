package org.multiverse.instrumentation.annotations;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.commit;
import org.multiverse.api.Handle;
import org.multiverse.api.annotations.NonEscaping;
import org.multiverse.api.annotations.TmEntity;
import org.multiverse.multiversionedstm.MultiversionedStm;
import org.multiverse.multiversionedstm.MultiversionedStm.MultiversionedTransaction;

public class ReadLazyAndSelfManagedTest {
    private MultiversionedStm stm;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
    }

    @Test
    public void testNormal() {
        Normal normal = new Normal();
        normal.field = new Field();
        Handle<Normal> handle = commit(stm, normal);

        MultiversionedTransaction t = stm.startTransaction();
        int oldCount = t.getReferenceMapSize();
        Normal found = t.read(handle);
        Field field1 = found.get();
        int newCount = t.getReferenceMapSize();
        assertEquals(oldCount + 2, newCount);

        Field field2 = found.get();
        assertEquals(oldCount + 2, t.getReferenceMapSize());

        assertSame(field1, field2);
    }

    @TmEntity
    public static class Normal {
        private Field field;

        public Field get() {
            return field;
        }
    }

    @TmEntity
    public static class Field {
        private int value;
    }

    @Test
    public void testReadLazyAndSelfManaged() {
        LazyAndSelfManaged original = new LazyAndSelfManaged();
        original.create();
        Handle<LazyAndSelfManaged> handle = commit(stm, original);

        MultiversionedTransaction t = stm.startTransaction();

        //load the main reference and see that the field is not loaded yet.
        LazyAndSelfManaged found = t.read(handle);
        assertEquals(1, t.getReferenceMapSize());
        int oldSize = t.getReferenceMapSize();

        //load the field
        Field field1 = found.get();

        //check that the field is not stored in the transaction.
        assertNotNull(field1);
        int newSize = t.getReferenceMapSize();
        assertEquals(oldSize, newSize);
    }

    /**
     * This example is not a good example of a SelfManagedField since the object
     * is able to escape the outer object.
     */
    @TmEntity
    public static class LazyAndSelfManaged {

        @NonEscaping
        private Field field;

        public void create() {
            field = new Field();
        }

        public Field get() {
            return field;
        }
    }
}
