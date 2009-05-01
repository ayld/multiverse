package org.multiverse.multiversionedstm;

import static org.junit.Assert.*;
import org.junit.Test;
import org.multiverse.api.Handle;
import org.multiverse.api.TransactionId;
import org.multiverse.api.exceptions.SnapshotTooOldException;
import org.multiverse.api.exceptions.TooManyRetriesException;
import org.multiverse.api.exceptions.WriteConflictException;
import org.multiverse.multiversionedstm.DefaultHandle.State;
import org.multiverse.multiversionedstm.examples.IntegerValue;
import org.multiverse.multiversionedstm.examples.Stack;
import org.multiverse.util.Bag;
import org.multiverse.util.RetryCounter;

public class DefaultHandleTest {

    private DefaultHandle createCommitted(long version) {
        return createCommitted(new Stack(), version);
    }

    private DefaultHandle createCommitted(MaterializedObject materializedObject, long version) {
        DefaultHandle object = new DefaultHandle();
        TransactionId id = new TransactionId();
        if (!object.tryAcquireLockForWriting(id, 0, new RetryCounter(1)))
            fail();

        //todo: bag
        object.writeAndReleaseLock(id, materializedObject.dematerialize(), version, new Bag());
        return object;
    }


    // ============== tryAcquireLockForWriting ==================

    @Test
    public void tryToAcquireLockForWritingSucceedsIfLockIsFreeAndThereIsNoCommittedState() {
        long version = 10;
        DefaultHandle stmObject = new DefaultHandle();
        TransactionId lockOwner = new TransactionId();

        boolean success = stmObject.tryAcquireLockForWriting(lockOwner, version, new RetryCounter(1));

        assertTrue(success);
        State state = stmObject.getState();
        assertNotNull(state);
        assertSame(lockOwner, state.lockOwner);
        assertNull(state.dematerialized);
        assertNull(state.listenerHead);
    }

    @Test
    public void tryToAcquireLockForWritingSucceedsIfLockIsFreeAndThereIsCommittedState() {
        long version = 10;
        DefaultHandle stmObject = createCommitted(version);
        TransactionId lockOwner = new TransactionId();
        State oldState = stmObject.getState();

        boolean success = stmObject.tryAcquireLockForWriting(lockOwner, version, new RetryCounter(1));

        assertTrue(success);
        State newState = stmObject.getState();
        assertNotNull(newState);
        assertSame(lockOwner, newState.lockOwner);
        assertSame(oldState.listenerHead, newState.listenerHead);
        assertSame(oldState.dematerialized, newState.dematerialized);
    }

    @Test
    public void tryToAcquireLockForWritingFailsIsLockIsNotFreeAndThereIsCommittedState() {
        long version = 10;
        DefaultHandle handle = createCommitted(version);

        TransactionId transactionId1 = new TransactionId();
        handle.tryAcquireLockForWriting(transactionId1, version, new RetryCounter(1));
        State oldState = handle.getState();

        TransactionId transactionId2 = new TransactionId();
        boolean success = handle.tryAcquireLockForWriting(transactionId2, version, new RetryCounter(1));

        assertFalse(success);
        assertSame(oldState, handle.getState());
    }

    @Test
    public void tryToAcquireLockForWritingFailsIsLockIsNotFreeAndThereIsNoCommittedState() {
        DefaultHandle handle = new DefaultHandle();

        TransactionId transactionId1 = new TransactionId();
        handle.tryAcquireLockForWriting(transactionId1, 0, new RetryCounter(1));

        State oldState = handle.getState();

        TransactionId transactionId2 = new TransactionId();
        boolean success = handle.tryAcquireLockForWriting(transactionId2, 0, new RetryCounter(1));

        assertFalse(success);
        assertSame(oldState, handle.getState());
    }

    @Test
    public void tryToAcquireLockForWritingFailsIfWriteConflictIsDetected() {
        long version = 10;
        DefaultHandle handle = createCommitted(version);

        State oldState = handle.getState();

        TransactionId transactionId2 = new TransactionId();
        try {
            handle.tryAcquireLockForWriting(transactionId2, version - 1, new RetryCounter(1));
            fail();
        } catch (WriteConflictException er) {
        }

        assertSame(oldState, handle.getState());
    }

    // ============== writeAndReleaseLock ==================
    @Test
    public void releaseLockForWritingSucceeds() {
        long version = 10;
        DefaultHandle stmObject = createCommitted(version);
        TransactionId lockOwner = new TransactionId();

        stmObject.tryAcquireLockForWriting(lockOwner, version, new RetryCounter(1));
        stmObject.releaseLockForWriting(lockOwner);

        State state = stmObject.getState();
        assertNotNull(state);
        assertNull(state.lockOwner);
    }

    @Test
    public void releaseLockForWritingIsIgnoredIfTheTransactionIdDoesntMatch() {
        long version = 10;
        DefaultHandle stmObject = createCommitted(version);
        TransactionId lockOwner = new TransactionId();

        stmObject.tryAcquireLockForWriting(lockOwner, version, new RetryCounter(1));

        TransactionId other = new TransactionId();

        stmObject.releaseLockForWriting(other);

        State state = stmObject.getState();
        assertNotNull(state);
        assertSame(lockOwner, state.lockOwner);
    }

    //=======================================

    @Test
    public void getDehydratedSearchingForMatchingVersion() {
        long materializeVersion = 3;
        long searchVersion = 3;

        getDehydrated(materializeVersion, searchVersion);
    }

    @Test
    public void getDehydratedSearchingForNewerVersion() {
        long materializeVersion = 3;
        long searchVersion = 4;

        getDehydrated(materializeVersion, searchVersion);
    }

    public void getDehydrated(long materializeVersion, long searchVersion) {
        MaterializedObject object = new IntegerValue();
        Handle handle = object.getHandle();
        DematerializedObject dematerialized = object.dematerialize();
        TransactionId transactionId = new TransactionId();
        handle.tryAcquireLockForWriting(transactionId, 0, new RetryCounter(1));
        handle.writeAndReleaseLock(transactionId, dematerialized, materializeVersion, new Bag());

        DematerializedObject found = handle.tryRead(searchVersion, new RetryCounter(1));
        assertSame(dematerialized, found);
    }


    @Test
    public void getDehydratedFailsIfOlderVersionOfDataDoesNotExist() {
        long dematerializeVersion = 3;
        long searchVersion = 2;

        TransactionId owner = new TransactionId();
        MaterializedObject materializedObject = new IntegerValue();
        Handle handle = materializedObject.getHandle();
        DematerializedObject dematerialized = materializedObject.dematerialize();

        handle.tryAcquireLockForWriting(owner, 0, new RetryCounter(1));
        handle.writeAndReleaseLock(owner, dematerialized, dematerializeVersion, new Bag());

        try {
            handle.tryRead(searchVersion, new RetryCounter(1));
            fail();
        } catch (SnapshotTooOldException e) {

        }
    }

    //============== tryGetLastCommitted ========================

    //@Test

    public void getLastCommited() {
        MaterializedObject object = new IntegerValue(45);
        Handle handle = object.getHandle();
        TransactionId id = new TransactionId();
        handle.tryAcquireLockForWriting(id, 0, new RetryCounter(1));
        DematerializedObject dematerializedObject = object.dematerialize();
        handle.writeAndReleaseLock(id, dematerializedObject, 100, new Bag());

        RetryCounter retryCounter = new RetryCounter(1);
        DematerializedObject found = handle.tryGetLastCommitted(retryCounter);
        assertSame(dematerializedObject, found);
    }

    @Test
    public void getLastCommitedOfNonCommittedObsionject() {
        DefaultHandle handle = new DefaultHandle();

        RetryCounter retryCounter = new RetryCounter(1);
        DematerializedObject found = handle.tryGetLastCommitted(retryCounter);
        assertNull(found);
    }

    @Test
    public void tryToGetLastCommitedFailsWhenLockedForWriting() {
        long version = 10;
        DefaultHandle handle = createCommitted(version);

        TransactionId transactionId = new TransactionId();
        if (!handle.tryAcquireLockForWriting(transactionId, version, new RetryCounter(1)))
            fail();

        RetryCounter retryCounter = new RetryCounter(50);
        try {
            handle.tryGetLastCommitted(retryCounter);
            fail();
        } catch (TooManyRetriesException error) {
        }
    }
}
