package org.codehaus.multiverse.multiversionedstm2;

import org.codehaus.multiverse.IllegalPointerException;
import org.codehaus.multiverse.Stm;
import org.codehaus.multiverse.transaction.Transaction;
import org.codehaus.multiverse.transaction.TransactionStatus;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

public class MultiversionedStm implements Stm {

    private final AtomicReference<HeapSnapshot> heapSnapshotAtomicReference = new AtomicReference(new HeapSnapshot());

    public Transaction startTransaction() {
        return new MultiversionedTransaction();
    }

    public long getActiveVersion() {
        return heapSnapshotAtomicReference.get().getVersion();
    }

    public Transaction startTransaction(Transaction predecessor) throws InterruptedException {
        throw new RuntimeException();
    }

    public Transaction tryStartTransaction(Transaction predecessor, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        throw new RuntimeException();
    }

    public class MultiversionedTransaction implements Transaction {

        private TransactionStatus status = TransactionStatus.active;
        private final Map<DehydratedCitizen, Citizen> hydratedMap = new IdentityHashMap();
        private final Map<Long, Citizen> localCitizenMap = new HashMap();
        private final HeapSnapshot heapSnapshot;

        public MultiversionedTransaction() {
            heapSnapshot = heapSnapshotAtomicReference.get();
        }

        public Citizen hydrate(DehydratedCitizen dehydrated) {
            if (status != TransactionStatus.active)
                throw new IllegalStateException();

            if (dehydrated == null)
                return null;

            Citizen citizen = hydratedMap.get(dehydrated);
            if (citizen != null)
                return citizen;

            citizen = dehydrated.hydrate();
            hydratedMap.put(dehydrated, citizen);
            return citizen;
        }

        public long attachRoot(Object root) {
            return 0;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public Object readRoot(long ptr) {
            if (ptr <= 0)
                throw new IllegalPointerException(ptr);

            if (status != TransactionStatus.active)
                throw new IllegalStateException();

            Citizen citizen = localCitizenMap.get(ptr);
            if (citizen != null)
                return citizen;

            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public void deleteRoot(long ptr) {
            throw new RuntimeException();
        }

        public TransactionStatus getStatus() {
            return status;
        }

        public void commit() {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        public void abort() {
            switch (status) {
                case active:
                    status = TransactionStatus.aborted;
                    break;
                case aborted:
                    break;
                case committed:
                    throw new IllegalStateException();
                default:
                    throw new RuntimeException();
            }
        }
    }
}
