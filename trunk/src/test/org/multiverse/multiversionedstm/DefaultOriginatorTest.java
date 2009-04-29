package org.multiverse.multiversionedstm;

import static org.junit.Assert.*;
import org.junit.Test;
import org.multiverse.api.Originator;
import org.multiverse.api.TransactionId;
import org.multiverse.api.exceptions.SnapshotTooOldException;
import org.multiverse.api.exceptions.TooManyRetriesException;
import org.multiverse.api.exceptions.WriteConflictException;
import org.multiverse.examples.IntegerValue;
import org.multiverse.examples.Stack;
import org.multiverse.multiversionedstm.DefaultOriginator.State;
import org.multiverse.util.RetryCounter;

public class DefaultOriginatorTest {

    private DefaultOriginator createCommitted(long version) {
        return createCommitted(new Stack(), version);
    }

    private DefaultOriginator createCommitted(MaterializedObject materializedObject, long version) {
        DefaultOriginator object = new DefaultOriginator();
        TransactionId id = new TransactionId();
        if (!object.tryAcquireLockForWriting(id, 0, new RetryCounter(1)))
            fail();
        object.writeAndReleaseLock(id, materializedObject.dematerialize(), version);
        return object;
    }


    // ============== tryAcquireLockForWriting ==================

    @Test
    public void tryToAcquireLockForWritingSucceedsIfLockIsFreeAndThereIsNoCommittedState() {
        long version = 10;
        DefaultOriginator stmObject = new DefaultOriginator();
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
        DefaultOriginator stmObject = createCommitted(version);
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
        DefaultOriginator originator = createCommitted(version);

        TransactionId transactionId1 = new TransactionId();
        originator.tryAcquireLockForWriting(transactionId1, version, new RetryCounter(1));
        State oldState = originator.getState();

        TransactionId transactionId2 = new TransactionId();
        boolean success = originator.tryAcquireLockForWriting(transactionId2, version, new RetryCounter(1));

        assertFalse(success);
        assertSame(oldState, originator.getState());
    }

    @Test
    public void tryToAcquireLockForWritingFailsIsLockIsNotFreeAndThereIsNoCommittedState() {
        DefaultOriginator originator = new DefaultOriginator();

        TransactionId transactionId1 = new TransactionId();
        originator.tryAcquireLockForWriting(transactionId1, 0, new RetryCounter(1));

        State oldState = originator.getState();

        TransactionId transactionId2 = new TransactionId();
        boolean success = originator.tryAcquireLockForWriting(transactionId2, 0, new RetryCounter(1));

        assertFalse(success);
        assertSame(oldState, originator.getState());
    }

    @Test
    public void tryToAcquireLockForWritingFailsIfWriteConflictIsDetected() {
        long version = 10;
        DefaultOriginator originator = createCommitted(version);

        State oldState = originator.getState();

        TransactionId transactionId2 = new TransactionId();
        try {
            originator.tryAcquireLockForWriting(transactionId2, version - 1, new RetryCounter(1));
            fail();
        } catch (WriteConflictException er) {
        }

        assertSame(oldState, originator.getState());
    }

    // ============== writeAndReleaseLock ==================
    @Test
    public void releaseLockForWritingSucceeds() {
        long version = 10;
        DefaultOriginator stmObject = createCommitted(version);
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
        DefaultOriginator stmObject = createCommitted(version);
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
        Originator originator = object.getOriginator();
        DematerializedObject dematerialized = object.dematerialize();
        TransactionId transactionId = new TransactionId();
        originator.tryAcquireLockForWriting(transactionId, 0, new RetryCounter(1));
        originator.writeAndReleaseLock(transactionId, dematerialized, materializeVersion);

        DematerializedObject found = originator.tryGetDehydrated(searchVersion, new RetryCounter(1));
        assertSame(dematerialized, found);
    }


    @Test
    public void getDehydratedFailsIfOlderVersionOfDataDoesNotExist() {
        long dematerializeVersion = 3;
        long searchVersion = 2;

        TransactionId owner = new TransactionId();
        MaterializedObject materializedObject = new IntegerValue();
        Originator originator = materializedObject.getOriginator();
        DematerializedObject dematerialized = materializedObject.dematerialize();

        originator.tryAcquireLockForWriting(owner, 0, new RetryCounter(1));
        originator.writeAndReleaseLock(owner, dematerialized, dematerializeVersion);

        try {
            originator.tryGetDehydrated(searchVersion, new RetryCounter(1));
            fail();
        } catch (SnapshotTooOldException e) {

        }
    }

    //============== tryGetLastCommitted ========================

    //@Test

    public void getLastCommited() {
        MaterializedObject object = new IntegerValue(45);
        Originator originator = object.getOriginator();
        TransactionId id = new TransactionId();
        originator.tryAcquireLockForWriting(id, 0, new RetryCounter(1));
        DematerializedObject dematerializedObject = object.dematerialize();
        originator.writeAndReleaseLock(id, dematerializedObject, 100);

        RetryCounter retryCounter = new RetryCounter(1);
        DematerializedObject found = originator.tryGetLastCommitted(retryCounter);
        assertSame(dematerializedObject, found);
    }

    @Test
    public void getLastCommitedOfNonCommittedObsionject() {
        DefaultOriginator originator = new DefaultOriginator();

        RetryCounter retryCounter = new RetryCounter(1);
        DematerializedObject found = originator.tryGetLastCommitted(retryCounter);
        assertNull(found);
    }

    @Test
    public void tryToGetLastCommitedFailsWhenLockedForWriting() {
        long version = 10;
        DefaultOriginator originator = createCommitted(version);

        TransactionId transactionId = new TransactionId();
        if (!originator.tryAcquireLockForWriting(transactionId, version, new RetryCounter(1)))
            fail();

        RetryCounter retryCounter = new RetryCounter(50);
        try {
            originator.tryGetLastCommitted(retryCounter);
            fail();
        } catch (TooManyRetriesException error) {
        }
    }
}
