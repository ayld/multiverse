package org.multiverse.stms.alpha.manualinstrumentation;

import org.multiverse.api.DirtinessStatus;
import org.multiverse.api.Tranlocal;
import org.multiverse.api.TranlocalSnapshot;
import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.ReadonlyException;
import org.multiverse.stms.alpha.mixins.FastAtomicObjectMixin;
import org.multiverse.templates.AtomicTemplate;
import static org.multiverse.api.StmUtils.retry;

public final class Stack<E> extends FastAtomicObjectMixin {

    public Stack() {
        new AtomicTemplate() {
            @Override
            public Object execute(Transaction t) {
                t.attachNew(new StackTranlocal(Stack.this));
                return null;
            }
        }.execute();
    }

    public int size() {
        return new AtomicTemplate<Integer>() {
            @Override
            public Integer execute(Transaction t) {
                StackTranlocal tranlocal = (StackTranlocal)t.privatize(Stack.this);
                return tranlocal.size();
            }
        }.execute();
    }

    public boolean isEmpty() {
        return new AtomicTemplate<Boolean>() {
            @Override
            public Boolean execute(Transaction t) {
                StackTranlocal tranlocal = (StackTranlocal)t.privatize(Stack.this);
                return tranlocal.isEmpty();
            }
        }.execute();
    }

    public void push(final E item) {
        new AtomicTemplate() {
            @Override
            public Integer execute(Transaction t) {
                StackTranlocal tranlocal = (StackTranlocal)t.privatize(Stack.this);
                tranlocal.push(item);
                return null;
            }
        }.execute();
    }

    public E pop() {
        return new AtomicTemplate<E>() {
            @Override
            public E execute(Transaction t) {
                StackTranlocal<E> tranlocal = (StackTranlocal)t.privatize(Stack.this);
                return tranlocal.pop();
            }
        }.execute();
    }

    public void clear() {
         new AtomicTemplate() {
            @Override
            public Integer execute(Transaction t) {
                StackTranlocal tranlocal = (StackTranlocal)t.privatize(Stack.this);
                tranlocal.clear();
                return null;
            }
        }.execute();
    }

    @Override
    public Tranlocal privatize(long version) {
        StackTranlocal<E> origin = (StackTranlocal<E>) load(version);
        return new StackTranlocal<E>(origin);
    }
}

final class StackTranlocal<E> extends Tranlocal {
    private final Stack<E> atomicObject;
    int size;
    Node<E> head;
    private StackTranlocal<E> origin;

    /**
     * Makes an initial version.
     *
     * @param atomicObject
     */
    StackTranlocal(Stack<E> atomicObject) {
        this.atomicObject = atomicObject;
        this.version = Long.MIN_VALUE;
    }

    /**
     * Makes a clone.
     *
     * @param origin
     */
    StackTranlocal(StackTranlocal<E> origin) {
        this.origin = origin;
        this.atomicObject = origin.atomicObject;
        this.size = origin.size;
        this.head = origin.head;
        this.version = origin.version;
    }

    @Override
    public Object getAtomicObject() {
        return atomicObject;
    }

    public void clear() {
        if (committed) {
            throw new ReadonlyException();
        }

        size = 0;
        head = null;
    }

    public void push(E item) {
        if (committed) {
            throw new ReadonlyException();
        }

        if (item == null) {
            throw new NullPointerException();
        }

        head = new Node<E>(head, item);
        size++;
    }

    public E pop() {
        if (committed) {
            throw new ReadonlyException();
        }

        if (size == 0) {
            retry();
        }

        size--;
        Node<E> oldHead = head;
        head = oldHead.next;
        return oldHead.value;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    static class Node<E> {
        final Node<E> next;
        final E value;

        Node(Node<E> next, E value) {
            this.next = next;
            this.value = value;
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
        throw new RuntimeException();
    }

    @Override
    public DirtinessStatus getDirtinessStatus() {
        if (committed) {
            return DirtinessStatus.committed;
        } else if (origin == null) {
            return DirtinessStatus.fresh;
        } else if (origin.size != this.size) {
            return DirtinessStatus.dirty;
        } else if (origin.head != this.head) {
            return DirtinessStatus.dirty;
        } else {
            return DirtinessStatus.clean;
        }
    }
}