package org.multiverse.instrumentation;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.commit;
import org.multiverse.api.Handle;
import org.multiverse.api.Transaction;
import org.multiverse.api.annotations.TmEntity;
import org.multiverse.multiversionedstm.DefaultMultiversionedHandle;
import org.multiverse.multiversionedstm.MaterializedObject;
import org.multiverse.multiversionedstm.MultiversionedStm;

public class TmEntityClassTransformer_Method_GetHandleTest {
    private MultiversionedStm stm;

    @Before
    public void setUp() {
        stm = new MultiversionedStm();
    }

    @Test
    public void freshObject() {
        SomeEntity entity = new SomeEntity();
        Handle handle = ((MaterializedObject) entity).getHandle();
        assertNotNull(handle);
        assertTrue(handle instanceof DefaultMultiversionedHandle);
    }

    @Test
    public void returnedHandleOnAttach() {
        SomeEntity entity = new SomeEntity();

        Transaction t = stm.startTransaction();
        Handle<SomeEntity> handle = t.attach(entity);
        assertSame(handle, ((MaterializedObject) entity).getHandle());
    }

    @Test
    public void readObject() {
        SomeEntity entity = new SomeEntity();
        Handle<SomeEntity> handle = commit(stm, entity);

        Transaction t = stm.startTransaction();
        SomeEntity found = t.read(handle);
        assertSame(handle, ((MaterializedObject) found).getHandle());
    }

    @TmEntity
    public static class SomeEntity {

    }
}
