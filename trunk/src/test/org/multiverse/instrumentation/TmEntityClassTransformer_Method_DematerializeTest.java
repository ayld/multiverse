package org.multiverse.instrumentation;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.commit;
import org.multiverse.api.GlobalStmInstance;
import org.multiverse.api.Handle;
import org.multiverse.api.Stm;
import org.multiverse.api.Transaction;
import org.multiverse.api.annotations.TmEntity;
import org.multiverse.multiversionedstm.MaterializedObject;

public class TmEntityClassTransformer_Method_DematerializeTest {
    private Stm stm;

    @Before
    public void setUp() {
        stm = GlobalStmInstance.getInstance();
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
    public void testNonTmObjectMember() {
        IntegerMember original = new IntegerMember();
        original.member = 100000;

        Handle<IntegerMember> handle = commit(stm, original);
        Transaction t = stm.startTransaction();
        IntegerMember found = t.read(handle);
        assertEquals(original.member, found.member);
    }

    @Test
    public void testNonTmObjectMemberThatIsNull() {
        IntegerMember original = new IntegerMember();
        original.member = null;

        Handle<IntegerMember> handle = commit(stm, original);
        Transaction t = stm.startTransaction();
        IntegerMember found = t.read(handle);
        assertEquals(original.member, found.member);
    }

    @TmEntity
    public static class IntegerMember {
        public Integer member;

        public IntegerMember() {
        }

        public IntegerMember(Integer member) {
            this.member = member;
        }
    }

    @Test
    public void testTmMemberIsNotNull() {
        TmMember original = new TmMember();
        original.member = new IntegerMember(10);

        Handle<TmMember> handle = commit(stm, original);
        Transaction t = stm.startTransaction();
        TmMember found = t.read(handle);
        assertEquals(original.member.member, found.getMember().member);
    }

    @Test
    public void testTmMemberIsNull() {
        TmMember original = new TmMember();
        original.member = null;

        Handle<TmMember> handle = commit(stm, original);
        Transaction t = stm.startTransaction();
        TmMember found = t.read(handle);
        assertNull(found.getMember());
    }

    @TmEntity
    public static class TmMember {
        public IntegerMember member;

        public IntegerMember getMember() {
            return member;
        }
    }

    //todo: @Test
    public void testRuntimeTmMember() {

    }

    @Test
    public void testRuntimeNonTmMember() {
        ObjectMember original = new ObjectMember();
        original.member = new Integer(10);

        Handle<ObjectMember> handle = commit(stm, original);
        Transaction t = stm.startTransaction();
        ObjectMember found = t.read(handle);
        assertSame(original.member, found.member);
    }

    @TmEntity
    public static class ObjectMember {
        public Object member;
    }
}
