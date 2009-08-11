package org.multiverse.api.locks;

import org.multiverse.api.Tranlocal;

/**
 * A {@link org.multiverse.api.locks.LockManager} that is deactivated. It can be used in '
 * locations where a LockManager is needed, but no locking is possible (a readonly exception
 * for example).
 *
 * It also provides an static INSTANCE, to reduce object creation overhead. No need to
 * create multiple instances.
 *
 * @author Peter Veentjer
 */
public class DeactivatedLockManager implements LockManager{

    public final static DeactivatedLockManager INSTANCE = new DeactivatedLockManager();

    @Override
    public LockStatus getLockStatus(Object atomicObject) {
        return null;  //todo
    }

    @Override
    public Tranlocal privatize(Object atomicObject, LockStatus lockMode) {
        return null;  //todo
    }

    @Override
    public StmLock getLock(Object atomicObject, LockStatus lockStatus) {
        return null;  //todo
    }
}
