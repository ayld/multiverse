package org.multiverse.stms.alpha.writeset;

import org.multiverse.api.Transaction;
import org.multiverse.stms.alpha.AlphaAtomicObject;

/**
 * A {@link WriteSetLockPolicy} that fails immediately when the locks can't be acquired. No spinning
 * or retrying whatsoever.
 *
 * @author Peter Veentjer
 */
public final class FailFastWriteSetLockPolicy implements WriteSetLockPolicy {

    public final static FailFastWriteSetLockPolicy INSTANCE = new FailFastWriteSetLockPolicy();

    @Override
    public boolean acquireLocks(WriteSet writeSet, Transaction owner) {
        WriteSet writeSetNode = writeSet;

        while (writeSetNode != null) {
            AlphaAtomicObject atomicObject = writeSetNode.tranlocal.getAtomicObject();
            if (!atomicObject.acquireLock(owner)) {
                return false;
            }
            writeSetNode = writeSetNode.next;
        }

        return true;
    }
}
