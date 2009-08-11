package org.multiverse.stms.alpha.manualinstrumentation;

import org.multiverse.api.DirtinessStatus;
import org.multiverse.api.Tranlocal;
import org.multiverse.api.TranlocalSnapshot;
import org.multiverse.api.exceptions.ReadonlyException;
import org.multiverse.stms.alpha.manualinstrumentation.IntStackTranlocal.IntNode;
import static org.multiverse.api.StmUtils.retry;

public final class IntStackTranlocal extends Tranlocal {

    IntStack atomicObject;
    int size;
    IntNode head;
    IntStackTranlocal origin;

    IntStackTranlocal(IntStackTranlocal origin) {
        this.origin = origin;
        this.atomicObject = origin.atomicObject;
        this.version = origin.version;
        this.size = origin.size;
        this.head = origin.head;
    }

    IntStackTranlocal(IntStack atomicObject) {
        this.atomicObject = atomicObject;
    }

    @Override
    public Object getAtomicObject() {
        return atomicObject;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void push(int value) {
        if (committed) {
            throw new ReadonlyException();
        } else {
            head = new IntNode(value, head);
            size++;
        }
    }

    public int pop() {
        if (committed) {
            throw new ReadonlyException();
        } else {
            if (head == null) {
                retry();
            }

            size--;
            IntNode oldHead = head;
            head = oldHead.next;
            return oldHead.value;
        }
    }

    public static class IntNode {
        final int value;
        final IntNode next;

        IntNode(int value, IntNode next) {
            this.value = value;
            this.next = next;
        }
    }

    @Override
    public void prepareForCommit(long writeVersion) {
        this.version = writeVersion;
        this.committed = true;
        this.origin = null;
    }

    @Override
    public TranlocalSnapshot takeSnapshot() {
        return new TranlocalIntStackSnapshot(this);
    }

    @Override
    public DirtinessStatus getDirtinessStatus() {
        if (committed) {
            return DirtinessStatus.committed;
        } else if (origin == null) {
            return DirtinessStatus.fresh;
        } else if (origin.head != this.head) {
             return DirtinessStatus.dirty;
        } else {
            return DirtinessStatus.clean;
        }
    }
}

final class TranlocalIntStackSnapshot extends TranlocalSnapshot {
    public final IntStackTranlocal tranlocal;
    public final int size;
    public final IntNode head;

    TranlocalIntStackSnapshot(IntStackTranlocal tranlocal) {
        this.tranlocal = tranlocal;
        this.size = tranlocal.size;
        this.head = tranlocal.head;
    }

    @Override
    public Tranlocal getTranlocal() {
        return tranlocal;
    }

    @Override
    public void restore() {
        tranlocal.size = size;
        tranlocal.head = head;
    }
}