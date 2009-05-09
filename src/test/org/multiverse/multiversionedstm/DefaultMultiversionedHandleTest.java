package org.multiverse.multiversionedstm;

import static org.junit.Assert.*;
import org.junit.Test;
import org.multiverse.api.TransactionId;
import org.multiverse.api.exceptions.SnapshotTooOldException;
import org.multiverse.api.exceptions.StarvationException;
import org.multiverse.api.exceptions.WriteConflictException;
import org.multiverse.multiversionedstm.DefaultMultiversionedHandle.State;
import org.multiverse.multiversionedstm.examples.IntegerValue;
import org.multiverse.multiversionedstm.examples.Stack;
import org.multiverse.util.RetryCounter;

public class DefaultMultiversionedHandleTest {

    private DefaultMultiversionedHandle createCommitted(long version) {
        return createCommitted(new Stack(), version);
    }

    private DefaultMultiversionedHandle createCommitted(MaterializedObject materializedObject, long version) {
        DefaultMultiversionedHandle object = new DefaultMultiversionedHandle();
        TransactionId id = new TransactionId();
        object.tryToAcquireLocksForWritingAndDetectForConflicts(id, 0, new RetryCounter(1));

        //todo: bag
        object.writeAndReleaseLock(id, materializedObject.dematerialize(), version);
        return object;
    }


    // ============== tryToAcquireLocksForWritingAndDetectForConflicts ==================

    @Test
    public void tryToAcquireLockForWritingSucceedsIfLockIsFreeAndThereIsNoCommittedState() {
        long version = 10;
        DefaultMultiversionedHandle stmObject = new DefaultMultiversionedHandle();
        TransactionId lockOwner = new TransactionId();

        stmObject.tryToAcquireLocksForWritingAndDetectForConflicts(lockOwner, version, new RetryCounter(1));

        State state = stmObject.getState();
        assertNotNull(state);
        assertSame(lockOwner, state.lockOwner);
        assertNull(state.dematerializedObject);
        assertNull(state.listenerHead);
    }

    @Test
    public void tryToAcquireLockForWritingSucceedsIfLockIsFreeAndThereIsCommittedState() {
        long version = 10;
        DefaultMultiversionedHandle stmObject = createCommitted(version);
        TransactionId lockOwner = new TransactionId();
        State oldState = stmObject.getState();

        stmObject.tryToAcquireLocksForWritingAndDetectForConflicts(lockOwner, version, new RetryCounter(1));

        State newState = stmObject.getState();
        assertNotNull(newState);
        assertSame(lockOwner, newState.lockOwner);
        assertSame(oldState.listenerHead, newState.listenerHead);
        assertSame(oldState.dematerializedObject, newState.dematerializedObject);
    }

    @Test
    public void tryToAcquireLockForWritingFailsIsLockIsNotFreeAndThereIsCommittedState() {
        long version = 10;
        DefaultMultiversionedHandle handle = createCommitted(version);

        TransactionId transactionId1 = new TransactionId();
        handle.tryToAcquireLocksForWritingAndDetectForConflicts(transactionId1, version, new RetryCounter(1));
        State oldState = handle.getState();

        TransactionId transactionId2 = new TransactionId();
        try {
            handle.tryToAcquireLocksForWritingAndDetectForConflicts(transactionId2, version, new RetryCounter(1));
            fail();
        } catch (StarvationException ex) {

        }

        assertSame(oldState, handle.getState());
    }

    @Test
    public void tryToAcquireLockForWritingFailsIsLockIsNotFreeAndThereIsNoCommittedState() {
        DefaultMultiversionedHandle handle = new DefaultMultiversionedHandle();

        TransactionId transactionId1 = new TransactionId();
        handle.tryToAcquireLocksForWritingAndDetectForConflicts(transactionId1, 0, new RetryCounter(1));

        State oldState = handle.getState();

        TransactionId transactionId2 = new TransactionId();
        try {
            handle.tryToAcquireLocksForWritingAndDetectForConflicts(transactionId2, 0, new RetryCounter(1));
            fail();
        } catch (StarvationException ex) {

        }

        assertSame(oldState, handle.getState());
    }

    @Test
    public void tryToAcquireLockForWritingFailsIfWriteConflictIsDetected() {
        long version = 10;
        DefaultMultiversionedHandle handle = createCommitted(version);

        State oldState = handle.getState();

        TransactionId transactionId2 = new TransactionId();
        try {
            handle.tryToAcquireLocksForWritingAndDetectForConflicts(transactionId2, version - 1, new RetryCounter(1));
            fail();
        } catch (WriteConflictException er) {
        }

        assertSame(oldState, handle.getState());
    }

    // ============== writeAndReleaseLock ==================
    @Test
    public void releaseLockForWritingSucceeds() {
        long version = 10;
        DefaultMultiversionedHandle stmObject = createCommitted(version);
        TransactionId lockOwner = new TransactionId();

        stmObject.tryToAcquireLocksForWritingAndDetectForConflicts(lockOwner, version, new RetryCounter(1));
        stmObject.releaseLockForWriting(lockOwner);

        State state = stmObject.getState();
        assertNotNull(state);
        assertNull(state.lockOwner);
    }

    @Test
    public void releaseLockForWritingIsIgnoredIfTheTransactionIdDoesntMatch() {
        long version = 10;
        DefaultMultiversionedHandle stmObject = createCommitted(version);
        TransactionId lockOwner = new TransactionId();

        stmObject.tryToAcquireLocksForWritingAndDetectForConflicts(lockOwner, version, new RetryCounter(1));

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
        MultiversionedHandle handle = object.getHandle();
        DematerializedObject dematerialized = object.dematerialize();
        TransactionId transactionId = new TransactionId();
        handle.tryToAcquireLocksForWritingAndDetectForConflicts(transactionId, 0, new RetryCounter(1));
        handle.writeAndReleaseLock(transactionId, dematerialized, materializeVersion);

        DematerializedObject found = handle.tryRead(searchVersion, new RetryCounter(1));
        assertSame(dematerialized, found);
    }


    @Test
    public void getDehydratedFailsIfOlderVersionOfDataDoesNotExist() {
        long dematerializeVersion = 3;
        long searchVersion = 2;

        TransactionId owner = new TransactionId();
        MaterializedObject materializedObject = new IntegerValue();
        MultiversionedHandle handle = materializedObject.getHandle();
        DematerializedObject dematerialized = materializedObject.dematerialize();

        handle.tryToAcquireLocksForWritingAndDetectForConflicts(owner, 0, new RetryCounter(1));
        handle.writeAndReleaseLock(owner, dematerialized, dematerializeVersion);

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
        MultiversionedHandle handle = object.getHandle();
        TransactionId id = new TransactionId();
        handle.tryToAcquireLocksForWritingAndDetectForConflicts(id, 0, new RetryCounter(1));
        DematerializedObject dematerializedObject = object.dematerialize();
        handle.writeAndReleaseLock(id, dematerializedObject, 100);

        RetryCounter retryCounter = new RetryCounter(1);
        DematerializedObject found = handle.tryGetLastCommitted(retryCounter);
        assertSame(dematerializedObject, found);
    }

    @Test
    public void getLastCommitedOfNonCommittedObsionject() {
        DefaultMultiversionedHandle handle = new DefaultMultiversionedHandle();

        RetryCounter retryCounter = new RetryCounter(1);
        DematerializedObject found = handle.tryGetLastCommitted(retryCounter);
        assertNull(found);
    }

    @Test
    public void tryToGetLastCommitedFailsWhenLockedForWriting() {
        long version = 10;
        DefaultMultiversionedHandle handle = createCommitted(version);

        TransactionId transactionId = new TransactionId();
        handle.tryToAcquireLocksForWritingAndDetectForConflicts(transactionId, version, new RetryCounter(1));

        RetryCounter retryCounter = new RetryCounter(50);
        try {
            handle.tryGetLastCommitted(retryCounter);
            fail();
        } catch (StarvationException error) {
        }
    }
}
