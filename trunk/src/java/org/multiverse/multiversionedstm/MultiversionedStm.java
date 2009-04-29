package org.multiverse.multiversionedstm;

import org.multiverse.api.*;
import org.multiverse.api.exceptions.NoProgressPossibleException;
import org.multiverse.api.exceptions.SnapshotTooOldException;
import org.multiverse.api.exceptions.WriteConflictException;
import static org.multiverse.multiversionedstm.MultiversionedStmUtils.initializeNextChain;
import org.multiverse.util.RetryCounter;
import org.multiverse.util.latches.CheapLatch;
import org.multiverse.util.latches.Latch;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A multiversioned {@link Stm} implemention.
 *
 * @author Peter Veentjer.
 */
public final class MultiversionedStm implements Stm {

    private final AtomicLong globalVersionClock = new AtomicLong();
    private final MultiversionedStmStatistics statistics = new MultiversionedStmStatistics();

    public MultiversionedTransaction startTransaction() {
        statistics.incTransactionStartedCount();
        return new MultiversionedTransaction();
    }

    public MultiversionedStmStatistics getStatistics() {
        return statistics;
    }

    public long getGlobalVersion() {
        return globalVersionClock.get();
    }

    private class MultiversionedTransaction implements Transaction {
        private final HashMap<Originator, LazyReferenceImpl> referenceMap =
                new HashMap<Originator, LazyReferenceImpl>();
        private final TransactionId transactionId = new TransactionId();
        private final long readVersion = globalVersionClock.get();
        private volatile TransactionState state = TransactionState.active;
        private volatile String description;
        private MaterializedObject first;

        @Override
        public TransactionId getId() {
            return transactionId;
        }

        @Override
        public TransactionState getState() {
            return state;
        }

        @Override
        public void setDescription(String description) {
            this.description = description;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public void abort() {
            switch (state) {
                case active:
                    state = TransactionState.aborted;
                    statistics.incTransactionAbortedCount();
                    break;
                case aborted:
                    //ignore
                    break;
                case committed:
                    throw new IllegalStateException("Can't abort an already committed transaction");
                default:
                    throw new RuntimeException();
            }
        }

        @Override
        public Transaction abortAndRetry() throws InterruptedException {
            assertActive();
            abort();

            if (isNoProgressPossible())
                throw new NoProgressPossibleException();

            Latch listener = registerListener();
            awaitListenerToOpen(listener);
            statistics.incTransactionRetriedCount();
            return startTransaction();
        }

        private void awaitListenerToOpen(Latch listener) throws InterruptedException {
            statistics.incTransactionPendingRetryCount();
            try {
                listener.await();
            } finally {
                statistics.decTransactionPendingRetryCount();
            }
        }

        private Latch registerListener() {
            Latch listener = new CheapLatch();

            initFirst();
            MaterializedObject m = first;

            while (m != null) {
                if (!m.getOriginator().tryAddLatch(listener, readVersion + 1, new RetryCounter(100000)))
                    throw new RuntimeException();
                m = m.getNextInChain();
            }

            //for (LazyReferenceImpl ref : referenceMap.values()) {
            //     if (ref.isLoaded()) {
            //         //todo: tryCounter & error
            //         if (!ref.get().getOriginator().tryAddLatch(listener, readVersion + 1, new RetryCounter(100000)))
            //             throw new RuntimeException();
            //      }
            //  }
            return listener;
        }

        private boolean isNoProgressPossible() {
            return referenceMap.isEmpty();
        }

        private void assertActive() {
            if (state != TransactionState.active) {
                throw new IllegalStateException();
            }
        }

        @Override
        public <T> Originator<T> attach(T objectToAttach) {
            assertActive();

            if (objectToAttach == null) {
                throw new NullPointerException();
            }

            if (!(objectToAttach instanceof MaterializedObject)) {
                throw new IllegalArgumentException();
            }

            MaterializedObject materializedObjectToAttach = (MaterializedObject) objectToAttach;
            Originator<T> originator = materializedObjectToAttach.getOriginator();
            referenceMap.put(originator, new LazyReferenceImpl(materializedObjectToAttach));
            return originator;
        }

        @Override
        public <T> T read(Originator<T> originator) {
            LazyReference<T> ref = readLazy(originator);
            return ref == null ? null : ref.get();
        }

        @Override
        public <T> T readUnmanaged(Originator<T> originator) {
            assertActive();

            if (originator == null) {
                return null;
            }

            //todo: statistics, check the reference so that same logic is used
            DematerializedObject dematerialized = originator.tryGetDehydrated(
                    readVersion,
                    new RetryCounter(1000000000));

            T result = (T) dematerialized.rematerialize(this);
            statistics.incMaterializedCount();
            return result;
        }

        @Override
        public <T> LazyReference<T> readLazy(Originator<T> originator) {
            assertActive();

            if (originator == null) {
                return null;
            }

            LazyReferenceImpl ref = referenceMap.get(originator);
            if (ref == null) {
                ref = new LazyReferenceImpl(originator);
                referenceMap.put(originator, ref);
            }

            return ref;
        }

        @Override
        public <T> LazyReference<T> readLazyAndUnmanaged(Originator<T> originator) {
            assertActive();

            if (originator == null) {
                return null;
            }

            return new LazyReferenceImpl(originator);
        }

        private MaterializedObject[] createWriteSet() {
            if (first == null)
                return new MaterializedObject[]{};

            final List<MaterializedObject> writeSet = new LinkedList<MaterializedObject>();
            MaterializedObject m = first;

            while (m != null) {
                if (m.isDirty()) {
                    writeSet.add(m);
                }
                m = m.getNextInChain();
            }
            return writeSet.toArray(new MaterializedObject[writeSet.size()]);
        }

        @Override
        public void commit() {
            switch (state) {
                case active:
                    doCommit();
                    break;
                case committed:
                    //ignore
                    break;
                case aborted:
                    throw new IllegalStateException("Can't commit an already aborted transaction");
                default:
                    throw new RuntimeException();
            }
        }

        private void initFirst() {
            first = initializeNextChain(referenceMap.values().iterator());
        }

        private void doCommit() {
            try {
                initFirst();
                MaterializedObject[] writeSet = createWriteSet();
                if (writeSet.length == 0) {
                    //it is a readonly transaction
                    statistics.incTransactionReadonlyCount();
                } else {
                    DematerializedObject[] dematerializedWriteSet = dematerialize(writeSet);

                    //it is a transaction with writes.
                    boolean success = false;
                    do {
                        try {
                            if (tryToAcquireLocksForWritingAndDetectForConflicts(writeSet)) {
                                writeAndReleaseLocksForWriting(dematerializedWriteSet);
                                success = true;
                            }
                        } finally {
                            if (!success) {
                                releaseLocksForWriting(writeSet);
                                //Thread.yield();
                            }
                        }
                    } while (!success);
                }

                state = TransactionState.committed;
                statistics.incTransactionCommittedCount();
            } catch (RuntimeException e) {
                abort();
                throw e;
            }
        }

        private DematerializedObject[] dematerialize(MaterializedObject[] writeSet) {
            DematerializedObject[] result = new DematerializedObject[writeSet.length];
            for (int k = 0; k < writeSet.length; k++) {
                result[k] = writeSet[k].dematerialize();
            }
            return result;
        }

        /**
         * Tries to acquire the locks and do conflict detection.
         *
         * @param writeSet the MaterializedObjects to lock
         * @return true if the lock was a success, false it could not be completed
         * @throws WriteConflictException if a writeconflict was encountered.
         */
        private boolean tryToAcquireLocksForWritingAndDetectForConflicts(MaterializedObject[] writeSet) {
            try {
                //todo: externalize
                RetryCounter retryCounter = new RetryCounter(10);
                for (MaterializedObject obj : writeSet) {
                    Originator originator = obj.getOriginator();

                    if (!originator.tryAcquireLockForWriting(transactionId, readVersion, retryCounter)) {
                        statistics.incTransactionLockAcquireFailureCount();
                        return false;
                    }

                    statistics.incLockAcquiredCount();
                }

                return true;
            } catch (WriteConflictException e) {
                statistics.incTransactionWriteConflictCount();
                throw e;
            }
        }

        private void writeAndReleaseLocksForWriting(DematerializedObject[] writeSet) {
            long writeVersion = globalVersionClock.incrementAndGet();

            for (DematerializedObject dirtyObject : writeSet) {
                Originator originator = dirtyObject.getOriginator();
                originator.writeAndReleaseLock(transactionId, dirtyObject, writeVersion);
                statistics.incWriteCount();
            }
        }

        private void releaseLocksForWriting(MaterializedObject[] writeSet) {
            for (MaterializedObject dirtyObject : writeSet) {
                Originator originator = dirtyObject.getOriginator();
                originator.releaseLockForWriting(transactionId);
            }
        }

        private class LazyReferenceImpl<S extends MaterializedObject> implements LazyReference<S> {
            private final Originator originator;
            private S ref;

            LazyReferenceImpl(Originator originator) {
                assert originator != null;
                this.originator = originator;
            }

            LazyReferenceImpl(S materializedObject) {
                this.ref = materializedObject;
                this.originator = materializedObject.getOriginator();
            }

            @Override
            public Originator getOriginator() {
                return originator;
            }

            @Override
            public boolean isLoaded() {
                return ref != null;
            }

            @Override
            public S get() {
                if (!isLoaded()) {
                    assertActive();
                    //todo: try counter
                    DematerializedObject dematerialized;
                    try {
                        dematerialized = originator.tryGetDehydrated(readVersion, new RetryCounter(1000000000));
                    } catch (SnapshotTooOldException ex) {
                        statistics.incTransactionSnapshotTooOldCount();
                        throw ex;
                    }

                    ref = (S) dematerialized.rematerialize(MultiversionedTransaction.this);
                    statistics.incMaterializedCount();
                }

                return ref;
            }
        }
    }
}
