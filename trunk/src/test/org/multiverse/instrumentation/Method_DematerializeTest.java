package org.multiverse.instrumentation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.commit;
import org.multiverse.api.Handle;
import org.multiverse.api.TmEntity;
import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.MaterializedObject;
import org.multiverse.multiversionedstm.MultiversionedStm;

public class Method_DematerializeTest {
    private MultiversionedStm stm;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
    }

    @Test
    public void test() {
        Handle<NoContent> handle = commit(stm, new NoContent());

        Transaction t = stm.startTransaction();
        NoContent noContent = t.read(handle);
        assertSame(handle, ((MaterializedObject) noContent).getHandle());
    }

    @TmEntity
    public static class NoContent {
    }

    @Test
    public void testPrimitiveMembers() {
        PrimitiveMembers original = new PrimitiveMembers();
        original.booleanField = true;
        original.byteField = 10;
        original.shortField = 25;
        original.intField = 10000;
        original.floatField = 23.5f;
        original.longField = 1000l;
        original.doubleField = 2004.4;
        original.charField = 'a';

        Handle<PrimitiveMembers> handle = commit(stm, original);
        Transaction t = stm.startTransaction();
        PrimitiveMembers found = t.read(handle);
        assertEquals(original.booleanField, found.booleanField);
        assertEquals(original.byteField, found.byteField);
        assertEquals(original.shortField, found.shortField);
        assertEquals(original.intField, found.intField);
        assertEquals(original.floatField, found.floatField, 0.0000001);
        assertEquals(original.longField, found.longField);
        assertEquals(original.doubleField, found.doubleField, 0.00000001);
        assertEquals(original.charField, found.charField);
    }

    @TmEntity
    public static class PrimitiveMembers {
        public boolean booleanField;
        public byte byteField;
        public short shortField;
        public int intField;
        public float floatField;
        public long longField;
        public double doubleField;
        public char charField;
    }

    @Test
    public void testObjectMemberThatIsNotTmEntity() {
        IntegerContent content = new IntegerContent();
        content.integer = 100000;

        Handle<IntegerContent> handle = commit(stm, content);
        Transaction t = stm.startTransaction();
        IntegerContent found = t.read(handle);
        assertEquals(content.integer, found.integer);
    }

    @TmEntity
    public static class IntegerContent {
        public Integer integer;
    }
}
