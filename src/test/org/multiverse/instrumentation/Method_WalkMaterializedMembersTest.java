package org.multiverse.instrumentation;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Ignore;
import org.multiverse.api.TmEntity;
import org.multiverse.multiversionedstm.MaterializedObject;
import org.multiverse.multiversionedstm.MemberWalker;
import org.multiverse.multiversionedstm.MultiversionedStm;

import static java.util.Arrays.asList;
import java.util.LinkedList;
import java.util.List;

public class Method_WalkMaterializedMembersTest {

    private MultiversionedStm stm;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
    }

    @Test
    public void objectWithoutMembers() {
        NoMembers noMembers = new NoMembers();
        assertWalkedMembers(noMembers);
    }

    @TmEntity
    static class NoMembers {
    }

    @Test
    public void objectWithANonTmEntityMember() {
        IntegerMember integerMember = new IntegerMember();
        integerMember.member = 10;
        assertWalkedMembers(integerMember);
    }

    @TmEntity
    static class IntegerMember {
        protected Integer member;
    }

    //todo: @Test
    public void objectWithRuntimeKnownNonTmMember() {
        ObjectMember objectMember = new ObjectMember();
        objectMember.member = 10;
        assertWalkedMembers(objectMember);
    }

    //todo: @Test
    public void objectWithRuntimeKnownTmMember() {
        ObjectMember objectMember = new ObjectMember();
        objectMember.member = new NoMembers();
        assertWalkedMembers(objectMember, objectMember.member);
    }

    @TmEntity
    static class ObjectMember {
        protected Object member;
    }

    @Test
    public void objectWithOneMember() {
        OneMember oneMember = new OneMember();
        oneMember.member = new NoMembers();
        assertWalkedMembers(oneMember, oneMember.member);
    }

    @Test
    public void nullMembersAreIgnored() {
        OneMember oneMember = new OneMember();
        assertWalkedMembers(oneMember);
    }

    @TmEntity
    static class OneMember {
        protected NoMembers member;
    }

    @Test
    public void objectWithMultipleMembers() {
        MultipleMembers multipleMembers = new MultipleMembers();
        multipleMembers.member1 = new NoMembers();
        multipleMembers.member2 = new NoMembers();
        multipleMembers.member3 = new NoMembers();
        assertWalkedMembers(multipleMembers, multipleMembers.member1, multipleMembers.member2, multipleMembers.member3);
    }

    @TmEntity
    static class MultipleMembers {
        protected NoMembers member1;
        protected NoMembers member2;
        protected NoMembers member3;
    }

    @Test
    public void staticMembersAreIgnored() {
        StaticMember staticMember = new StaticMember();
        StaticMember.noMembers = new NoMembers();

        assertWalkedMembers(staticMember);
    }

    @TmEntity
    static class StaticMember {
        protected static NoMembers noMembers;
    }

    //todo: @Test
    public void ignoredMembersAreIgnored() {
        IgnoredMember ignoredMember = new IgnoredMember();
        ignoredMember.members = new NoMembers();
        assertWalkedMembers(ignoredMember);
    }

    @TmEntity
    static class IgnoredMember {
        @Ignore
        protected NoMembers members;
    }

    private void assertWalkedMembers(Object materializedObject, Object... members) {
        assertTrue(materializedObject instanceof MaterializedObject);
        MemberWalkerImpl walker = new MemberWalkerImpl();
        ((MaterializedObject) materializedObject).walkMaterializedMembers(walker);
        assertEquals(asList(members), walker.memberList);
    }

    private static class MemberWalkerImpl implements MemberWalker {
        List memberList = new LinkedList();

        @Override
        public void onMember(MaterializedObject member) {
            memberList.add(member);
        }
    }
}
