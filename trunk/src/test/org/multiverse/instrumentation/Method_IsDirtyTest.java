package org.multiverse.instrumentation;

import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.commit;
import org.multiverse.api.Handle;
import org.multiverse.api.TmEntity;
import org.multiverse.api.Transaction;
import org.multiverse.collections.Stack;
import static org.multiverse.instrumentation.InstrumentationTestSupport.assertIsDirty;
import org.multiverse.multiversionedstm.MultiversionedStm;

public class Method_IsDirtyTest {
    private MultiversionedStm stm;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
    }

    @Test
    public void initialObjectIsDirty() {
        Stack stack = new Stack();
        assertIsDirty(stack, true);
    }

    @Test
    public void objectWithoutState() {

    }


    @TmEntity
    public static class NoMembers {

    }

    @Test
    public void staticMembersAreIgnored() {

    }

    @TmEntity
    public static class StaticMember {
        static NoMembers member;
    }

    @Test
    public void ignoredMembersAreIgnored() {

    }

    //@Test
    public void readObjectIsNotDirty() {
        Handle<Stack> handle = commit(stm, new Stack());

        Transaction t = stm.startTransaction();
        Stack found = t.read(handle);
        assertIsDirty(found, false);
    }

}
