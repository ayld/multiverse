package org.multiverse.stms.alpha.mixins;

import org.multiverse.MultiverseConstants;
import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.LoadLockedException;
import org.multiverse.api.exceptions.LoadTooOldVersionException;
import org.multiverse.api.exceptions.PanicError;
import org.multiverse.stms.alpha.AlphaAtomicObject;
import org.multiverse.stms.alpha.AlphaTranlocal;
import org.multiverse.utils.Listeners;
import org.multiverse.utils.latches.Latch;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * todo:
 * does the trick with read(content), read(transaction), read(content) really work?
 *
 * @author Peter Veentjer
 */
public abstract class FastAtomicObjectMixin implements AlphaAtomicObject {

    private final static AtomicReferenceFieldUpdater<FastAtomicObjectMixin, Transaction> lockOwnerUpdater =
            AtomicReferenceFieldUpdater.newUpdater(FastAtomicObjectMixin.class, Transaction.class, "lockOwner");

    private final static AtomicReferenceFieldUpdater<FastAtomicObjectMixin, AlphaTranlocal> tranlocalUpdater =
            AtomicReferenceFieldUpdater.newUpdater(FastAtomicObjectMixin.class, AlphaTranlocal.class, "tranlocal");

    private final static AtomicReferenceFieldUpdater<FastAtomicObjectMixin, Listeners> listenersUpdater =
            AtomicReferenceFieldUpdater.newUpdater(FastAtomicObjectMixin.class, Listeners.class, "listeners");

    private volatile Transaction lockOwner;
    private volatile AlphaTranlocal tranlocal;
    private volatile Listeners listeners;

    @Override
    public AlphaTranlocal load() {
        return tranlocalUpdater.get(this);
    }

    @Override
    public final AlphaTranlocal load(long readVersion) {
        AlphaTranlocal tranlocalT1 = tranlocalUpdater.get(this);

        if (tranlocalT1 == null) {
            //a read is done, but there is no committed data. Lets return null.
            return null;
        }

        if (MultiverseConstants.SANITY_CHECK_ENABLED) {
            if (!tranlocalT1.committed) {
                throw new PanicError();
            }
        }
        //since the version is directly connected to the tranlocal, we don't need to worry that we
        //see tranlocal with a wrong version. Something you need to watch out for if they are stored
        //in seperate (cas)fields.

        //If the tranlocal is exactly the one we look for, it doesn't matter if it is still locked. It is
        //going to be committed eventually and waiting for it, or retrying the transaction, would have
        //no extra value.

        if (tranlocalT1.version == readVersion) {
            //we are lucky, the tranlocal is exactly the one we are looking for.
            return tranlocalT1;
        } else if (tranlocalT1.version > readVersion) {
            //the current tranlocal it too new to return, so we fail. In the future this would
            //be the location to search for tranlocal with the correct version.
            throw LoadTooOldVersionException.create();
        } else {
            Transaction lockOwner = lockOwnerUpdater.get(this);

            if (lockOwner != null) {
                //todo: this would be the location for spinning. As long as the lock is there,
                //we are not sure
                throw LoadLockedException.create();
            }

            AlphaTranlocal tranlocalT2 = tranlocalUpdater.get(this);
            boolean otherWritesHaveBeenExecuted = tranlocalT2 != tranlocalT1;
            if (otherWritesHaveBeenExecuted) {
                //if the tranlocal has changed, lets check if the new tranlocal has exactly the
                //version we are looking for.
                if (tranlocalT2.version == readVersion) {
                    return tranlocalT2;
                }

                //we were not able to find the version we are looking for. It could be tranlocalT1
                //or tranlocalT2 but it could also have been a write we didn't notice. So lets
                //fails to indicate that we didn't find it.
                throw LoadTooOldVersionException.create();
            } else {
                //the tranlocal has not changed and it was unlocked. This means that we read
                //an old version that we can use.
                return tranlocalT1;
            }
        }
    }

    @Override
    public Transaction getLockOwner() {
        return lockOwner;
    }

    @Override
    public final boolean tryLock(Transaction lockOwner) {
        return lockOwnerUpdater.compareAndSet(this, null, lockOwner);
    }

    @Override
    public final void releaseLock(Transaction expectedLockOwner) {
        lockOwnerUpdater.compareAndSet(this, expectedLockOwner, null);
    }

    @Override
    public final void storeAndReleaseLock(AlphaTranlocal tranlocal, long writeVersion) {
        if (MultiverseConstants.SANITY_CHECK_ENABLED) {
            if (lockOwner == null) {
                throw new PanicError();
            }

            if (tranlocal.version >= writeVersion) {
                throw new PanicError();
            }

            AlphaTranlocal old = tranlocalUpdater.get(this);
            if (old != null) {
                if (old.version >= writeVersion) {
                    throw new PanicError();
                }
            }
        }

        //it is very important that the tranlocal write is is done before the lock release.
        //it also is very important that the commit and version are set, before the tranlocal write.
        tranlocal.prepareForCommit(writeVersion);

        tranlocalUpdater.set(this, tranlocal);

        //it is important that the listeners are removed after the tranlocal write en before the lockrelease.
        Listeners listeners = listenersUpdater.getAndSet(this, null);

        //de store geeft de garantie dat alle listeners die zijn geplaats voor de lock is geacquired
        //worden geopened. De store geeft zelfs de garantie dat alle listeners die zijn geplaatst voordat
        //de write heeft plaats gevonden worden geopend.
        lockOwnerUpdater.set(this, null);

        if (listeners != null) {
            listeners.openAll();
        }
    }

    @Override
    public final boolean registerRetryListener(Latch listener, long minimumVersion) {
        AlphaTranlocal tranlocalT1 = tranlocalUpdater.get(this);

        //could it be that a locked value is read? (YES, can happen) A value that will be updated,
        //but isn't updated yet.. consequence: the listener tries to register a listener.
        if (tranlocalT1 == null) {
            //no tranlocal has been committed yet. We don't need to register the listener,
            //because this call can only be made a transaction that has newattached items
            //and does an abort.

            //todo: one thing we still need to take care of is the noprogresspossible exception
            //if all objects within the transaction give this behavior it looks like the
            //latch was registered, but the transaction will never be woken up.
            return false;
        } else if (tranlocalT1.version >= minimumVersion) {
            //if the version if the tranlocal already is equal or bigger than the version we
            //are looking for, we are done.
            listener.open();
            return true;
        } else {
            //ok, the version we are looking for has not been committed yet, so we need to
            //register a the listener so that it will be openend

            boolean placedListener;
            Listeners newListeners;
            Listeners oldListeners;
            do {
                oldListeners = listenersUpdater.get(this);
                newListeners = new Listeners(listener, oldListeners);
                placedListener = listenersUpdater.compareAndSet(this, oldListeners, newListeners);
                if (!placedListener) {
                    //it could be that another transaction did a register, but it also could mean
                    //that a write occurred.
                    AlphaTranlocal tranlocalT2 = tranlocalUpdater.get(this);
                    if (tranlocalT1 != tranlocalT2) {
                        //we are not sure when the registration took place, but a new version is available.

                        if (MultiverseConstants.SANITY_CHECK_ENABLED) {
                            if (tranlocalT2.version <= tranlocalT1.version) {
                                throw new PanicError();
                            }

                            //deze fout dit zich voor, hoe kan dit gebeuren?
                            //is het een onredelijk eis die hier ligt?

                            //als de write nog niet heeft plaats gevonden, maar de clock is al wel opgehoogd,
                            //als je dan een lezende transactie hebt, kan die al de nieuwe clock zien en gaat
                            //dan starten. Als deze dan een retry gaat uitvoeren, dan ligt je dus

                            if (minimumVersion > tranlocalT2.version) {
                                //long dif = Math.abs(minimumVersion - tranlocalT2.version);
                                //System.out.println("difference " + dif);

                                throw new PanicError();
                            }
                        }
                        //a write happened so we can open this latch
                        listener.open();
                        return true;
                    }
                }
            } while (!placedListener);

            AlphaTranlocal tranlocalT2 = tranlocalUpdater.get(this);
            if (tranlocalT1 != tranlocalT2) {
                if (MultiverseConstants.SANITY_CHECK_ENABLED) {
                    //we are not sure when the registration took place, but a new version is available.
                    if (tranlocalT2.version < minimumVersion) {
                        throw new PanicError();
                    }
                }
                listener.open();
                //lets try to restore the oldListeners.
                listenersUpdater.compareAndSet(this, newListeners, oldListeners);
            } else {
                //it is registered before the write took place
            }

            return true;
        }
    }

    @Override
    public final boolean ensureConflictFree(long readVersion) {
        //since the lock is acquired....  is this really true? What about the readset? For the writeset
        //this is true.

        AlphaTranlocal tranlocal = tranlocalUpdater.get(this);

        if (tranlocal == null) {
            return true;
        }

        if (MultiverseConstants.SANITY_CHECK_ENABLED) {
            if (lockOwner == null) {
                throw new PanicError();
            }
        }

        return tranlocal.version <= readVersion;
    }
}
