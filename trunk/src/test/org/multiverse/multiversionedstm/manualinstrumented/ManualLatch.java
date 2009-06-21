package org.multiverse.multiversionedstm.manualinstrumented;

import static org.multiverse.api.StmUtils.retry;
import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.*;

import static java.lang.String.format;

/**
 * A Latch is a concurrency structure that acts like a door. While it is closed, transactions can wait for it
 * to open. Once it is opened, transactions can continue. Once it is opened, it remains openend.
 * <p/>
 * Latches are very useful for letting transactions/threads wait until something completes.
 *
 * @author Peter Veentjer.
 */
public final class ManualLatch implements MaterializedObject {

    private boolean isOpen;

    public ManualLatch() {
        this(false);
    }

    public ManualLatch(boolean isOpen) {
        this.isOpen = isOpen;
        this.handle = new DefaultMultiversionedHandle<ManualLatch>();
    }

    public void awaitOpen() {
        if (!isOpen)
            retry();
    }

    public void open() {
        isOpen = true;
    }

    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public String toString() {
        return format("Latch(isOpen=%s)", isOpen);
    }

    // ================== generated ====================

    private final MultiversionedHandle<ManualLatch> handle;
    private DematerializedLatch lastDematerialized;

    public ManualLatch(DematerializedLatch dematerializedLatch) {
        this.handle = dematerializedLatch.handle;
        this.isOpen = dematerializedLatch.isOpen;
        this.lastDematerialized = dematerializedLatch;
    }

    @Override
    public MultiversionedHandle<ManualLatch> getHandle() {
        return handle;
    }

    @Override
    public boolean isDirty() {
        if (lastDematerialized == null)
            return true;

        if (lastDematerialized.isOpen != isOpen)
            return true;

        return false;
    }

    @Override
    public DematerializedObject dematerialize() {
        return lastDematerialized = new DematerializedLatch(this);
    }

    @Override
    public void walkMaterializedMembers(MemberWalker memberWalker) {
        //do nothing
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

    static class DematerializedLatch implements DematerializedObject {

        private final MultiversionedHandle<ManualLatch> handle;
        private final boolean isOpen;

        DematerializedLatch(ManualLatch latch) {
            this.handle = latch.handle;
            this.isOpen = latch.isOpen;
        }

        @Override
        public MultiversionedHandle<ManualLatch> getHandle() {
            return handle;
        }

        @Override
        public MaterializedObject rematerialize(Transaction t) {
            return new ManualLatch(this);
        }
    }
}