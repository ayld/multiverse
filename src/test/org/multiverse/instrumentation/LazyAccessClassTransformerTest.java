package org.multiverse.instrumentation;

import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;
import static org.multiverse.TestUtils.assertMaterializedCount;
import static org.multiverse.TestUtils.commit;
import org.multiverse.api.GlobalStmInstance;
import org.multiverse.api.Handle;
import org.multiverse.api.Transaction;
import org.multiverse.api.annotations.TmEntity;
import org.multiverse.multiversionedstm.MultiversionedStm;

public class LazyAccessClassTransformerTest {
    private MultiversionedStm stm;

    @Before
    public void setUp() {
        stm = (MultiversionedStm) GlobalStmInstance.getInstance();
    }

    @Test
    public void memberIsAvailable() {
        TmObject object = new TmObject();
        object.setIntValue(new IntValue(1));

        Handle<TmObject> handle = commit(object);

        long materializedCount = stm.getStatistics().getMaterializedCount();
        Transaction t = stm.startTransaction();
        TmObject found = t.read(handle);

        assertMaterializedCount(stm, materializedCount + 1);

        IntValue foundIntValue = found.getIntValue();
        assertMaterializedCount(stm, materializedCount + 2);
    }

    @Test
    public void memberIsNoAvailable() {
        TmObject object = new TmObject();

        Handle<TmObject> handle = commit(object);

        long materializedCount = stm.getStatistics().getMaterializedCount();
        Transaction t = stm.startTransaction();
        TmObject found = t.read(handle);

        assertMaterializedCount(stm, materializedCount + 1);

        IntValue foundIntValue = found.getIntValue();
        assertNull(foundIntValue);
        assertMaterializedCount(stm, materializedCount + 1);
    }

    @TmEntity
    public static class TmObject {
        IntValue intValue;

        public IntValue getIntValue() {
            return intValue;
        }

        public void setIntValue(IntValue intValue) {
            this.intValue = intValue;
        }
    }
}
