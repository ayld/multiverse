package org.multiverse.multiversionedstm.manualinstrumented;

import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.*;

import static java.lang.String.format;

public final class ManualIntValue implements MaterializedObject {

    private int value;

    public ManualIntValue() {
        this.value = 0;
        this.handle = new DefaultMultiversionedHandle<ManualIntValue>();
    }

    public ManualIntValue(int value) {
        this.handle = new DefaultMultiversionedHandle<ManualIntValue>();
        this.value = value;
    }

    public int get() {
        return value;
    }

    public void set(int value) {
        this.value = value;
    }

    public void inc() {
        value++;
    }

    public void dec() {
        value--;
    }

    @Override
    public String toString() {
        return format("IntegerValue(value=%s)", value);
    }

    @Override
    public int hashCode() {
        return value;
    }

    @Override
    public boolean equals(Object thatObj) {
        if (thatObj == this)
            return true;

        if (!(thatObj instanceof ManualIntValue))
            return false;

        ManualIntValue that = (ManualIntValue) thatObj;
        return that.value == this.value;
    }

    // ========================= generated ======================================

    private DematerializedIntegerValue lastDematerialized;
    private final MultiversionedHandle handle;

    private ManualIntValue(DematerializedIntegerValue dematerializedIntegerValue) {
        this.lastDematerialized = dematerializedIntegerValue;
        this.handle = dematerializedIntegerValue.handle;
        this.value = dematerializedIntegerValue.value;
    }

    @Override
    public DematerializedIntegerValue dematerialize() {
        return lastDematerialized = new DematerializedIntegerValue(this);
    }

    private MaterializedObject nextInChain;

    @Override
    public MaterializedObject getNextInChain() {
        return nextInChain;
    }

    @Override
    public void setNextInChain(MaterializedObject next) {
        this.nextInChain = next;
    }

    @Override
    public void walkMaterializedMembers(MemberWalker memberWalker) {
    }

    @Override
    public MultiversionedHandle<ManualIntValue> getHandle() {
        return handle;
    }

    @Override
    public boolean isDirty() {
        if (lastDematerialized == null)
            return true;

        if (lastDematerialized.value != value)
            return true;

        return false;
    }

    private static class DematerializedIntegerValue implements DematerializedObject {
        private final MultiversionedHandle<ManualIntValue> handle;
        private final int value;

        private DematerializedIntegerValue(ManualIntValue intValue) {
            this.handle = intValue.handle;
            this.value = intValue.value;
        }

        @Override
        public MaterializedObject rematerialize(Transaction t) {
            return new ManualIntValue(this);
        }

        @Override
        public MultiversionedHandle<ManualIntValue> getHandle() {
            return handle;
        }
    }
}

