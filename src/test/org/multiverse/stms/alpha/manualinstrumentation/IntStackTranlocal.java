package org.multiverse.stms.alpha.manualinstrumentation;

import static org.multiverse.api.StmUtils.retry;
import org.multiverse.api.exceptions.ReadonlyException;
import org.multiverse.stms.alpha.AlphaAtomicObject;
import org.multiverse.stms.alpha.AlphaTranlocal;
import org.multiverse.stms.alpha.AlphaTranlocalSnapshot;
import org.multiverse.stms.alpha.DirtinessStatus;
import org.multiverse.stms.alpha.manualinstrumentation.IntStackTranlocal.IntNode;

public final class IntStackTranlocal extends AlphaTranlocal {

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
    public AlphaAtomicObject getAtomicObject() {
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
    public AlphaTranlocalSnapshot takeSnapshot() {
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

final class TranlocalIntStackSnapshot extends AlphaTranlocalSnapshot {
    public final IntStackTranlocal tranlocal;
    public final int size;
    public final IntNode head;

    TranlocalIntStackSnapshot(IntStackTranlocal tranlocal) {
        this.tranlocal = tranlocal;
        this.size = tranlocal.size;
        this.head = tranlocal.head;
    }

    @Override
    public AlphaTranlocal getTranlocal() {
        return tranlocal;
    }

    @Override
    public void restore() {
        tranlocal.size = size;
        tranlocal.head = head;
    }
}