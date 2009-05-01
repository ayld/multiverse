package org.multiverse.multiversionedstm.examples;

import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.*;

import static java.lang.String.format;

public final class IntegerValue implements MaterializedObject {

    private int value;

    public IntegerValue() {
        this(0);
    }

    public IntegerValue(int value) {
        this.handle = new DefaultHandle<IntegerValue>();
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

        if (!(thatObj instanceof IntegerValue))
            return false;

        IntegerValue that = (IntegerValue) thatObj;
        return that.value == this.value;
    }

    // ========================= generated ======================================

    private DematerializedIntegerValue lastDematerialized;
    private final MultiversionedHandle handle;

    private IntegerValue(DematerializedIntegerValue dematerializedIntegerValue) {
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
    public MultiversionedHandle<IntegerValue> getHandle() {
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

    public static class DematerializedIntegerValue implements DematerializedObject {
        private final MultiversionedHandle<IntegerValue> handle;
        private final int value;

        private DematerializedIntegerValue(IntegerValue source) {
            this.handle = source.getHandle();
            this.value = source.value;
        }

        @Override
        public MaterializedObject rematerialize(Transaction t) {
            return new IntegerValue(this);
        }

        @Override
        public MultiversionedHandle<IntegerValue> getHandle() {
            return handle;
        }
    }
}

