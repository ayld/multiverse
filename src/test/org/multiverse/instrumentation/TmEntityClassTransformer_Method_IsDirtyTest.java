package org.multiverse.instrumentation;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.SharedStmInstance;
import static org.multiverse.TestUtils.commit;
import org.multiverse.api.Handle;
import org.multiverse.api.Stm;
import org.multiverse.api.Transaction;
import org.multiverse.api.annotations.TmEntity;
import org.multiverse.collections.Stack;
import static org.multiverse.instrumentation.InstrumentationTestSupport.assertDirty;
import static org.multiverse.instrumentation.InstrumentationTestSupport.assertNotDirty;

public class TmEntityClassTransformer_Method_IsDirtyTest {
    private Stm stm;

    @Before
    public void setUp() {
        stm = SharedStmInstance.getInstance();
    }

    @Test
    public void initialObjectIsDirty() {
        Stack stack = new Stack();
        assertDirty(stack, true);
    }

    @Test
    public void objectWithoutState() {
        NoMembers noMembers = new NoMembers();
        Handle<NoMembers> handle = commit(noMembers);

        Transaction t = stm.startTransaction();
        assertNotDirty(t.read(handle));
    }

    @TmEntity
    public static class NoMembers {
    }

    //================= boolean =====================

    @Test
    public void booleanMember() {
        BooleanMember original = new BooleanMember();
        assertDirty(original);

        Handle<BooleanMember> handle = commit(original);
        Transaction t = stm.startTransaction();
        BooleanMember found = t.read(handle);
        assertNotDirty(found);

        found.member = !found.member;
        assertDirty(found);
    }

    @TmEntity
    public static class BooleanMember {
        private boolean member;
    }

    //================= short =====================

    @Test
    public void shortMember() {
        ShortMember original = new ShortMember();
        original.member = 10;
        assertDirty(original);

        Handle<ShortMember> handle = commit(original);
        Transaction t = stm.startTransaction();

        ShortMember found = t.read(handle);
        assertNotDirty(found);

        found.member = 11;
        assertDirty(found);

        found.member = 10;
        assertNotDirty(found);
    }

    @TmEntity
    public static class ShortMember {
        short member;
    }

    //================= char =====================

    @Test
    public void charMember() {
        CharMember original = new CharMember();
        original.member = 'a';
        assertDirty(original);

        Handle<CharMember> handle = commit(original);
        Transaction t = stm.startTransaction();
        CharMember found = t.read(handle);
        assertNotDirty(found);

        found.member = 'b';
        assertDirty(found);

        found.member = 'a';
        assertNotDirty(found);
    }

    @TmEntity
    public static class CharMember {
        char member;
    }

    //================= byte =====================

    @Test
    public void byteMember() {
        ByteMember original = new ByteMember();
        original.member = 10;
        assertDirty(original);

        Handle<ByteMember> handle = commit(original);
        Transaction t = stm.startTransaction();

        ByteMember found = t.read(handle);
        assertNotDirty(found);

        found.member = 11;
        assertDirty(found);

        found.member = 10;
        assertNotDirty(found);
    }

    @TmEntity
    public static class ByteMember {
        byte member;
    }

    //================= int =====================

    @Test
    public void intMember() {
        IntMember original = new IntMember();
        original.member = 10;
        assertDirty(original);

        Handle<IntMember> handle = commit(original);
        Transaction t = stm.startTransaction();

        IntMember found = t.read(handle);
        assertNotDirty(found);

        found.member = 11;
        assertDirty(found);

        found.member = 10;
        assertNotDirty(found);
    }

    @TmEntity
    public static class IntMember {
        int member;
    }

    //================= long =====================

    @Test
    public void longMember() {
        LongMember original = new LongMember();
        original.member = 10;
        assertDirty(original);

        Handle<LongMember> handle = commit(original);
        Transaction t = stm.startTransaction();

        LongMember found = t.read(handle);
        assertNotDirty(found);

        found.member = 11;
        assertDirty(found);

        found.member = 10;
        assertNotDirty(found);
    }

    @TmEntity
    public static class LongMember {
        long member;
    }

    @Test
    public void multipleLongMembers() {
        //todo
    }

    //================= float =====================

    @Test
    public void floatMember() {
        FloatMember original = new FloatMember();
        original.member = 10f;
        assertDirty(original);

        Handle<FloatMember> handle = commit(original);
        Transaction t = stm.startTransaction();
        FloatMember found = t.read(handle);
        assertNotDirty(found);

        found.member = 11f;
        assertDirty(found);

        found.member = 10f;
        assertNotDirty(found);
    }

    @TmEntity
    public static class FloatMember {
        float member;
    }

    //================= double =====================

    @Test
    public void doubleMember() {
        DoubleMember original = new DoubleMember();
        original.member = 10;
        assertDirty(original);

        Handle<DoubleMember> handle = commit(original);
        Transaction t = stm.startTransaction();
        DoubleMember found = t.read(handle);
        assertNotDirty(found);

        found.member = 11;
        assertDirty(found);

        //todo: set back to 10.
    }

    @TmEntity
    public static class DoubleMember {
        double member;
    }

    @Test
    public void multipleDoubleMembers() {
        //todo
    }

    public static class MultipleDoubleMembers {
        //todo
    }

    //================= static =====================

    @Test
    public void staticMembersAreIgnored() {
        //todo
    }

    @TmEntity
    public static class StaticMember {
        static NoMembers member;
    }

    @Test
    public void membersWithExcludedAnnotationAreIgnored() {
        //todo
    }

    @Test
    public void normalObjectMember() {
        //todo
    }

    //todo: instance == and equals 

    @Test
    public void tmEntityMember() {
        TmEntityMember original = new TmEntityMember();
        assertDirty(original);

        Handle<TmEntityMember> handle = commit(original);
        Transaction t = stm.startTransaction();

        TmEntityMember found = t.read(handle);
        assertNotDirty(found);

        found.setMember(null);
        assertNotDirty(found);

        found.setMember(new IntValue(10));
        assertDirty(found);

        found.setMember(null);
        assertNotDirty(found);
    }

    //todo: references.

    @TmEntity
    public class TmEntityMember {
        IntValue member;

        public IntValue getMember() {
            return member;
        }

        public void setMember(IntValue intValue) {
            this.member = intValue;
        }
    }
}
