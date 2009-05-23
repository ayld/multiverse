package org.multiverse.multiversionedstm;

import org.multiverse.api.*;
import org.multiverse.api.exceptions.NoProgressPossibleException;
import org.multiverse.api.exceptions.SnapshotTooOldException;
import org.multiverse.api.exceptions.StarvationException;
import org.multiverse.api.exceptions.WriteConflictException;
import static org.multiverse.multiversionedstm.MultiversionedStmUtils.initializeNextChain;
import org.multiverse.util.Bag;
import org.multiverse.util.ListenerNode;
import org.multiverse.util.RetryCounter;
import org.multiverse.util.latches.CheapLatch;
import org.multiverse.util.latches.Latch;

import static java.lang.String.format;
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
    private final MultiversionedStmStatistics statistics;

    public MultiversionedStm() {
        this(new MultiversionedStmStatistics());
    }

    public MultiversionedStm(MultiversionedStmStatistics statistics) {
        this.statistics = statistics;
    }

    public MultiversionedTransaction startTransaction() {
        if (statistics != null) {
            statistics.incTransactionStartedCount();
        }

        return new MultiversionedTransaction();
    }

    public MultiversionedStmStatistics getStatistics() {
        return statistics;
    }

    public long getGlobalVersion() {
        return globalVersionClock.get();
    }

    public class MultiversionedTransaction implements Transaction {
        private final HashMap<Handle, LazyReferenceImpl> referenceMap =
                new HashMap<Handle, LazyReferenceImpl>(2);

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

        /**
         * Returns the number of attached/read objects in this transaction.
         *
         * @return
         */
        public int getReferenceMapSize() {
            return referenceMap.size();
        }

        @Override
        public void abort() {
            switch (state) {
                case active:
                    state = TransactionState.aborted;
                    if (statistics != null) {
                        statistics.incTransactionAbortedCount();
                    }
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
            assertProgressPossible();

            Latch listener = registerListener();
            awaitListenerToOpen(listener);
            if (statistics != null) {
                statistics.incTransactionRetriedCount();
            }
            Transaction t = startTransaction();
            t.setDescription(description);
            return t;
        }

        private void assertProgressPossible() {
            if (isNoProgressPossible()) {
                throw new NoProgressPossibleException();
            }
        }

        private boolean isNoProgressPossible() {
            return referenceMap.isEmpty();
        }

        private void awaitListenerToOpen(Latch listener) throws InterruptedException {
            if (statistics != null) {
                statistics.incTransactionPendingRetryCount();
            }

            try {
                listener.await();
            } finally {
                if (statistics != null) {
                    statistics.decTransactionPendingRetryCount();
                }
            }
        }

        private Latch registerListener() {
            Latch listener = new CheapLatch();

            initFirst();
            MaterializedObject m = first;

            while (m != null) {
                if (!m.getHandle().tryAddLatch(listener, readVersion + 1, new RetryCounter(100))) {
                    //todo: in the statistics
                    throw StarvationException.INSTANCE;
                }
                m = m.getNextInChain();
            }

            return listener;
        }

        private void assertActive() {
            if (state != TransactionState.active) {
                String msg = format("Transaction should be active, but was %s", state);
                throw new IllegalStateException(msg);
            }
        }

        @Override
        public <T> Handle<T> attach(T objectToAttach) {
            assertActive();

            if (objectToAttach == null) {
                throw new NullPointerException("objectToAttach can't be null");
            }

            if (!(objectToAttach instanceof MaterializedObject)) {
                String msg = format("Can't attach %s, it is not a MaterializedObject instance", objectToAttach);
                throw new IllegalArgumentException(msg);
            }

            MaterializedObject materializedObjectToAttach = (MaterializedObject) objectToAttach;
            Handle<T> handle = materializedObjectToAttach.getHandle();
            referenceMap.put(handle, new LazyReferenceImpl(materializedObjectToAttach));
            return handle;
        }

        @Override
        public <T> T read(Handle<T> handle) {
            LazyReference<T> ref = readLazy(handle);
            return ref == null ? null : ref.get();
        }

        @Override
        public <T> T readSelfManaged(Handle<T> handle) {
            assertActive();

            if (handle == null) {
                return null;
            }

            DematerializedObject dematerializedObject = getDematerialized((MultiversionedHandle) handle);
            T result = (T) dematerializedObject.rematerialize(this);
            if (statistics != null) {
                statistics.incMaterializedCount();
            }
            return result;
        }

        @Override
        public <T> LazyReference<T> readLazy(Handle<T> handle) {
            System.out.println("readLazy");

            assertActive();

            if (handle == null) {
                return null;
            }

            LazyReferenceImpl ref = referenceMap.get(handle);
            if (ref == null) {
                ref = new LazyReferenceImpl((MultiversionedHandle) handle);
                referenceMap.put(handle, ref);
            }

            return ref;
        }

        @Override
        public <T> LazyReference<T> readLazyAndSelfManaged(Handle<T> handle) {
            System.out.println("readLazyAndSelfManaged");

            assertActive();

            if (handle == null) {
                return null;
            }

            return new LazyReferenceImpl((MultiversionedHandle) handle);
        }

        private MaterializedObject[] createWriteSet() {
            if (first == null) {
                return new MaterializedObject[]{};
            }

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
                    try {
                        doCommit();
                    } catch (RuntimeException e) {
                        abort();
                        throw e;
                    }
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
            initFirst();
            MaterializedObject[] writeSet = createWriteSet();
            if (writeSet.length == 0) {
                //it is a readonly transaction

                if (statistics != null) {
                    statistics.incTransactionReadonlyCount();
                }
            } else {
                //it is a transaction with writes.
                DematerializedObject[] dematerializedWriteSet = dematerialize(writeSet);
                boolean success = false;
                do {
                    try {
                        tryAcquireLocksForWritingAndDetectWriteForConflicts(writeSet);
                        writeAndReleaseLocksForWriting(dematerializedWriteSet);
                        success = true;
                    } catch (StarvationException e) {
                        //in case of a starvation exception, we try again.
                        //in the future this needs to be externalized using some kind of starvation policy.
                        if (statistics != null) {
                            statistics.incTransactionLockAcquireFailureCount();
                        }
                    } finally {
                        if (!success) {
                            releaseLocksForWriting(writeSet);
                        }
                    }
                } while (!success);
            }

            state = TransactionState.committed;

            if (statistics != null) {
                statistics.incTransactionCommittedCount();
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
         * @throws WriteConflictException if a writeconflict was encountered.
         * @throws StarvationException    if the locks could not be acquired because the transaction was starved.
         */
        private void tryAcquireLocksForWritingAndDetectWriteForConflicts(MaterializedObject[] writeSet) {
            int count = 0;
            RetryCounter retryCounter = new RetryCounter(0);

            try {
                for (int k = 0; k < writeSet.length; k++) {
                    MaterializedObject obj = writeSet[k];
                    MultiversionedHandle handle = obj.getHandle();

                    handle.tryToAcquireLockForWritingAndDetectForConflicts(transactionId, readVersion, retryCounter);
                    count++;
                }
            } catch (WriteConflictException e) {
                if (statistics != null) {
                    statistics.incTransactionWriteConflictCount();
                    statistics.incLockAcquiredCount(count);
                }

                throw e;
            }
        }

        private void writeAndReleaseLocksForWriting(DematerializedObject[] writeSet) {
            long writeVersion = globalVersionClock.incrementAndGet();

            Bag<ListenerNode> listeners = new Bag<ListenerNode>();

            for (int k = 0; k < writeSet.length; k++) {
                DematerializedObject dirtyObject = writeSet[k];
                ListenerNode listenerHead = dirtyObject.getHandle().writeAndReleaseLock(
                        transactionId,
                        dirtyObject,
                        writeVersion);

                if (listenerHead != null) {
                    listeners.add(listenerHead);
                }
            }

            while (!listeners.isEmpty()) {
                ListenerNode listenerNode = listeners.takeAny();
                listenerNode.openAll();
            }

            if (statistics != null) {
                statistics.incWriteCount(writeSet.length);
            }
        }

        private void releaseLocksForWriting(MaterializedObject[] writeSet) {
            for (MaterializedObject dirtyObject : writeSet) {
                MultiversionedHandle handle = dirtyObject.getHandle();
                handle.releaseLockForWriting(transactionId);
            }
        }

        private DematerializedObject getDematerialized(MultiversionedHandle handle) {
            DematerializedObject dematerialized;
            try {
                dematerialized = handle.tryRead(readVersion, new RetryCounter(1));
            } catch (SnapshotTooOldException ex) {
                if (statistics != null) {
                    statistics.incTransactionSnapshotTooOldCount();
                }

                throw ex;
            }
            return dematerialized;
        }

        private final class LazyReferenceImpl<S extends MaterializedObject> implements LazyReference<S> {
            private final MultiversionedHandle handle;
            private S ref;

            LazyReferenceImpl(MultiversionedHandle handle) {
                assert handle != null;
                this.handle = handle;
            }

            LazyReferenceImpl(S materializedObject) {
                this.ref = materializedObject;
                this.handle = materializedObject.getHandle();
            }

            @Override
            public MultiversionedHandle getHandle() {
                return handle;
            }

            @Override
            public boolean isLoaded() {
                return ref != null;
            }

            @Override
            public S get() {
                if (!isLoaded()) {
                    assertActive();
                    DematerializedObject dematerialized = getDematerialized(handle);

                    ref = (S) dematerialized.rematerialize(MultiversionedTransaction.this);
                    if (statistics != null) {
                        statistics.incMaterializedCount();
                    }
                }

                return ref;
            }
        }
    }
}
