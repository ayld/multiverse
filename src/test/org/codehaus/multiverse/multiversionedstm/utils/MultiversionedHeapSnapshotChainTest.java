package org.codehaus.multiverse.multiversionedstm.utils;

import org.junit.Before;
import org.junit.Test;

public class MultiversionedHeapSnapshotChainTest {

    private MultiversionedHeapSnapshotChain snapshotChain;

    @Before
    public void setUp() throws Exception {
        //    snapshotChain = new MultiversionedHeapSnapshotChain(null);
    }

    // ================ getHead() ===========================

    @Test
    public void testGetHead_initial() {
        //todo
    }


    // ================== get(long version) =================

    @Test
    public void testGet() {
        //todo
    }

    @Test
    public void testGetNonExistingVersion() {
        //todo
    }

    // ================== getSpecific(long version) ==========

    @Test
    public void testGetSpecific_success() {
        //todo
    }

    @Test
    public void testGetSpecific_noOldOneExist() {
        //todo
    }

    @Test
    public void testGetSpecific_doesntExist() {
        //todo
    }

    // ================= compareAndAdd ======================

    @Test
    public void testCompareAndAdd_success() {
        //todo
    }

    @Test
    public void testCompareAndSet_illegalVersion() {
        //todo
    }

    @Test
    public void testCompareAndAdd_null() {
        //todo
    }
}
