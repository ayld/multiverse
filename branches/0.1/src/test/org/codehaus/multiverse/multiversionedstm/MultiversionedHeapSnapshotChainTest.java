package org.codehaus.multiverse.multiversionedstm;

public class MultiversionedHeapSnapshotChainTest extends AbstractMultiversionedStmTest {

    private MultiversionedHeapSnapshotChain snapshotChain;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        //    snapshotChain = new MultiversionedHeapSnapshotChain(null);
    }

    // ================ getHead() ===========================

    public void testGetHead_initial() {
        //todo
    }


    // ================== get(long version) =================

    public void testGet() {
        //todo
    }

    public void testGetNonExistingVersion() {
        //todo
    }

    // ================== getSpecific(long version) ==========

    public void testGetSpecific_success() {
        //todo
    }

    public void testGetSpecific_noOldOneExist() {
        //todo
    }

    public void testGetSpecific_doesntExist() {
        //todo
    }

    // ================= compareAndAdd ======================

    public void testCompareAndAdd_success() {
        //todo
    }

    public void testCompareAndSet_illegalVersion() {
        //todo
    }

    public void testCompareAndAdd_null() {
        //todo
    }
}
