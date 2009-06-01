package org.multiverse.instrumentation;

import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.commit;
import org.multiverse.api.GlobalStmInstance;
import org.multiverse.api.Handle;
import org.multiverse.api.Stm;
import org.multiverse.api.Transaction;
import org.multiverse.api.annotations.Exclude;
import org.multiverse.api.annotations.TmEntity;
import static org.multiverse.instrumentation.InstrumentationTestSupport.assertDirty;
import static org.multiverse.instrumentation.InstrumentationTestSupport.assertNotDirty;
import org.multiverse.tmutils.LinkedTmStack;
import org.multiverse.tmutils.TmStack;

public class TmEntityClassTransformer_Method_IsDirtyTest {
    private Stm stm;

    @Before
    public void setUp() {
        stm = GlobalStmInstance.getInstance();
    }

    @Test
    public void initialObjectIsDirty() {
        TmStack stack = new LinkedTmStack();
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
        MultipleLongMembers original = new MultipleLongMembers();
        original.member1 = 10;
        original.member2 = 20;
        assertDirty(original);

        Handle<MultipleLongMembers> handle = commit(original);
        Transaction t = stm.startTransaction();

        MultipleLongMembers found = t.read(handle);
        assertNotDirty(found);

        found.member1 = 11;
        assertDirty(found);

        found.member1 = 10;
        assertNotDirty(found);

        found.member2 = 21;
        assertDirty(found);

        found.member2 = 20;
        assertNotDirty(found);
    }

    @TmEntity
    public static class MultipleLongMembers {
        long member1;
        long member2;
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
        double old = 10d;

        DoubleMember original = new DoubleMember();
        original.member = old;
        assertDirty(original);

        Handle<DoubleMember> handle = commit(original);
        Transaction t = stm.startTransaction();
        DoubleMember found = t.read(handle);
        assertNotDirty(found);

        System.out.println("member.double: " + found.member);

        found.member = 50d;
        assertDirty(found);

        found.member = old;
        assertNotDirty(found);
    }

    @TmEntity
    public static class DoubleMember {
        double member;
    }

    @Test
    public void multipleDoubleMembers() {
        MultipleDoubleMembers original = new MultipleDoubleMembers();
        original.member1 = 10;
        original.member2 = 20;
        assertDirty(original);

        Handle<MultipleDoubleMembers> handle = commit(original);
        Transaction t = stm.startTransaction();
        MultipleDoubleMembers found = t.read(handle);
        assertNotDirty(found);

        found.member1 = 11;
        assertDirty(found);

        found.member1 = 10;
        assertNotDirty(found);

        found.member2 = 21;
        assertDirty(found);

        found.member2 = 20;
        assertNotDirty(found);
    }

    @TmEntity
    public static class MultipleDoubleMembers {
        double member1;
        double member2;
    }

    //================= static =====================

    @Test
    public void staticMembersAreIgnored() {
        StaticMember original = new StaticMember();
        original.member = 10;
        assertDirty(original);

        Handle<StaticMember> handle = commit(original);
        Transaction t = stm.startTransaction();
        StaticMember found = t.read(handle);
        assertNotDirty(found);

        found.member = 20;
        assertNotDirty(found);
    }

    @TmEntity
    public static class StaticMember {
        static int member;
    }

    @Test
    public void membersWithExcludedAnnotationAreIgnored() {
        ExcludedMember original = new ExcludedMember();
        original.excludedMember = 10;
        original.nonExcludedMember = 20;
        assertDirty(original);

        Handle<ExcludedMember> handle = commit(original);
        Transaction t = stm.startTransaction();
        ExcludedMember found = t.read(handle);
        assertNotDirty(found);

        found.nonExcludedMember = 10000;
        assertDirty(found);

        found.nonExcludedMember = 20;
        assertNotDirty(found);

        found.excludedMember = 11;
        assertNotDirty(found);
    }

    @TmEntity
    public static class ExcludedMember {
        @Exclude
        int excludedMember;
        int nonExcludedMember;
    }

    @Test
    public void normalObjectMember() {
        String old = "foo";

        ObjectMember original = new ObjectMember();
        original.member = old;
        assertDirty(original);

        Handle<ObjectMember> handle = commit(original);
        Transaction t = stm.startTransaction();
        ObjectMember found = t.read(handle);
        assertNotDirty(found);

        found.member = "bar";
        assertDirty(found);

        found.member = old;
        assertNotDirty(found);

        //the dirty check should be on == and not on equals, so a equal string still indicates dirtyness
        found.member = new String("foo");
        assertDirty(found);
    }

    @TmEntity
    public static class ObjectMember {
        Object member;
    }

    @Test
    public void tmEntityMemberWithNullValue() {
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

    @Test
    public void tmEntityMemberWithNonValue() {
        TmEntityMember original = new TmEntityMember();
        original.member = new IntValue(10);
        assertDirty(original);

        Transaction t1 = stm.startTransaction();
        Handle<TmEntityMember> handle = t1.attach(original);
        Handle<IntValue> memberHandle = t1.attach(original.member);
        t1.commit();

        Transaction t2 = stm.startTransaction();

        TmEntityMember found = t2.read(handle);
        assertNotDirty(found);

        found.setMember(new IntValue(10));
        assertDirty(found);

        found.setMember(t2.read(memberHandle));
        assertNotDirty(found);
    }

    @Test
    public void memberDoesNotCountForDirtyness() {
        TmEntityMember original = new TmEntityMember();
        original.member = new IntValue(10);
        assertDirty(original);

        Handle<TmEntityMember> handle = commit(original);

        Transaction t = stm.startTransaction();
        TmEntityMember found = t.read(handle);

        found.getMember().inc();
        assertNotDirty(found);
        assertDirty(found.member);
    }

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
