package org.multiverse.instrumentation;

import static junit.framework.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.*;
import org.multiverse.api.Handle;
import org.multiverse.api.Transaction;
import org.multiverse.api.annotations.TmEntity;
import org.multiverse.multiversionedstm.MultiversionedStm;

/**
 * A test that checks if classes with suspicous fieldnames (so has fields with the same name as the
 * added files by the instrumentation) give no problems.
 *
 * @author Peter Veentjer.
 */
public class NoFieldNameConflictsTest {
    private MultiversionedStm stm;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
    }

    public <E> E commitAndRead(E item) {
        Handle<E> handle = commit(stm, item);
        return read(stm, handle);
    }

    @Test
    public void noConflictOnHandle() {
        HandleEntity original = new HandleEntity();
        original.handle = 10;

        HandleEntity found = commitAndRead(original);
        assertEquals(10, found.handle);
    }

    @TmEntity
    public static class HandleEntity {
        private int handle;
    }

    @Test
    public void noConflictOnLastMaterialized() {
        LastMaterializedEntity original = new LastMaterializedEntity();
        original.lastDematerialized = 10;

        LastMaterializedEntity found = commitAndRead(original);
        assertEquals(10, found.lastDematerialized);
    }

    @TmEntity
    public static class LastMaterializedEntity {
        private int lastDematerialized;
    }

    @Test
    public void noConflictOnNextInChain() {
        NextInChainEntity original = new NextInChainEntity();
        original.nextInChainEntity = 10;

        NextInChainEntity found = commitAndRead(original);
        assertEquals(10, found.nextInChainEntity);
    }

    @TmEntity
    public static class NextInChainEntity {
        private int nextInChainEntity;
    }

    @Test
    public void noConflictOnRef() {
        FooEntity foo = new FooEntity();
        ConflictingRefEntity original = new ConflictingRefEntity();
        original.foo = foo;

        Handle<ConflictingRefEntity> handle = commit(stm, original);
        Transaction t = stm.startTransaction();
        ConflictingRefEntity found = t.read(handle);
        assertSameHandle(foo, found.getFoo());
    }

    @TmEntity
    public static class ConflictingRefEntity {
        private FooEntity foo;
        private int fooRef;

        public FooEntity getFoo() {
            return foo;
        }
    }

    @TmEntity
    public static class FooEntity {

    }
}
