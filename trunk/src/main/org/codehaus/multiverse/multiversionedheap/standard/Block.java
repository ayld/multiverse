package org.codehaus.multiverse.multiversionedheap.standard;

import org.codehaus.multiverse.api.LockMode;
import org.codehaus.multiverse.api.TransactionId;
import org.codehaus.multiverse.multiversionedheap.Deflated;
import org.codehaus.multiverse.utils.latches.Latch;

import static java.lang.String.format;
import java.util.Stack;

/**
 * A BLock is the stuff that is stored in the heap. A Block contains the Deflated, but also contains
 * stuff like locking information and listener information. It would be possible to store the Deflated directly
 * in the heap, and this was the original design. But because the Inflatable implementation is the responsibility
 * of StmObject, it is hard (almost impossible) to add extra fields.
 * <p/>
 * A Block is immutable by nature.
 * <p/>
 *
 * @author Peter Veentjer.
 */
public class Block {
    private final Deflated deflated;
    private final LockMode lockMode;
    private final TransactionId lockOwner;
    private final ListenerNode listenerRoot;

    public Block(Deflated deflated, TransactionId lockOwner, LockMode lockMode, ListenerNode listenerRoot) {
        assert deflated != null;
        this.deflated = deflated;
        this.lockMode = lockMode;
        this.lockOwner = lockOwner;
        this.listenerRoot = listenerRoot;
    }

    public Block(Deflated deflated) {
        this(deflated, null, LockMode.free, null);
    }

    public ListenerNode getListenerRoot() {
        return listenerRoot;
    }

    public LockMode getLockMode() {
        return lockMode;
    }

    public TransactionId getLockOwner() {
        return lockOwner;
    }

    public long getHandle() {
        return deflated.___getHandle();
    }

    public Deflated getDeflated() {
        return deflated;
    }

    /**
     * @param newDeflated               the deflated that needs to be written
     * @param startOfTransactionVersion uses for writeconflict detection. If another transaction has committed
     *                                  after the current transaction has started, there is a write conflict. So
     *                                  if the version if equal or smaller than there is no write conflict.
     * @param listenersToOpen           a Stack where the listeners that need to be opened are stored.
     * @return the newly created Block or null if there was a writeconflict.
     */
    public Block createNewForUpdate(Deflated newDeflated, long startOfTransactionVersion, Stack<ListenerNode> listenersToOpen) {
        assert newDeflated != null && listenersToOpen != null;
        assert newDeflated.___getHandle() == deflated.___getHandle();

        if (deflated.___getVersion() > startOfTransactionVersion)
            return null;

        //the lock is released.
        //an the latches will be opened , so no need to transfer them along.
        if (listenerRoot != null)
            listenersToOpen.add(listenerRoot);

        return new Block(newDeflated, null, LockMode.free, null);
    }

    public Block createNewForAddingListener(Latch latch) {
        assert latch != null;

        if (latch.isOpen())
            return this;

        return new Block(deflated, lockOwner, lockMode, new ListenerNode(latch, listenerRoot));
    }

    public Block createNewForUpdatingLock(TransactionId newLockOwner, LockMode newLockMode) {
        assert newLockMode != null;

        if (this.lockOwner == null) {
            return createNewForFreeLock(newLockOwner, newLockMode);
        } else {
            return createNewForAlreadyHoldLock(newLockOwner, newLockMode);
        }
    }

    private Block createNewForAlreadyHoldLock(TransactionId newLockOwner, LockMode newLockMode) {
        if (newLockOwner == null) {
            //the lock needs to be unset
            return new Block(deflated, null, newLockMode, listenerRoot);
        } else if (this.lockOwner.equals(newLockOwner)) {
            return newLockMode.equals(lockMode) ? this : new Block(deflated, lockOwner, newLockMode, listenerRoot);
        } else {
            //somebody else wants to lock, lets indicate null to failure
            return null;
        }
    }

    private Block createNewForFreeLock(TransactionId newLockOwner, LockMode newLockMode) {
        if (newLockMode == LockMode.free)
            return this;

        return new Block(deflated, newLockOwner, newLockMode, listenerRoot);
    }

    @Override
    public String toString() {
        return format("Block(%s)", deflated);
    }
}