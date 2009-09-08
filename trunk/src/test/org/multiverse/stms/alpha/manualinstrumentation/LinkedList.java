package org.multiverse.stms.alpha.manualinstrumentation;

import static org.multiverse.api.StmUtils.retry;
import org.multiverse.api.Transaction;
import org.multiverse.api.annotations.AtomicMethod;
import org.multiverse.api.exceptions.LoadUncommittedException;
import org.multiverse.api.exceptions.ReadonlyException;
import org.multiverse.stms.alpha.*;
import org.multiverse.stms.alpha.mixins.FastAtomicObjectMixin;
import org.multiverse.templates.AtomicTemplate;
import org.multiverse.utils.TodoException;
import static org.multiverse.utils.TransactionThreadLocal.getThreadLocalTransaction;

import java.util.Iterator;
import java.util.NoSuchElementException;

final public class LinkedList<E> extends FastAtomicObjectMixin {

    public LinkedList() {
        new AtomicTemplate() {
            @Override
            public Object execute(Transaction t) throws Exception {
                ((AlphaTransaction) t).attachNew(new LinkedListTranlocal(LinkedList.this));
                return null;
            }
        }.execute();
    }

    @AtomicMethod
    public Iterator<E> iterator() {
        LinkedListTranlocal<E> tranlocal = ((LinkedListTranlocal<E>) AlphaStmUtils.load(this));
        return iterator(tranlocal);
    }

    @AtomicMethod
    public int getFirstIndexOf(E item) {
        LinkedListTranlocal<E> tranlocal = ((LinkedListTranlocal<E>) AlphaStmUtils.load(this));
        return getFirstIndexOf(tranlocal, item);
    }

    @AtomicMethod
    public E get(int index) {
        LinkedListTranlocal<E> tranlocal = ((LinkedListTranlocal<E>) AlphaStmUtils.load(this));
        return get(tranlocal, index);
    }

    @AtomicMethod
    public void clear() {
        LinkedListTranlocal<E> tranlocal = ((LinkedListTranlocal<E>) AlphaStmUtils.load(this));
        clear(tranlocal);
    }

    @AtomicMethod
    public E remove(int index) {
        LinkedListTranlocal<E> tranlocal = ((LinkedListTranlocal<E>) AlphaStmUtils.load(this));
        return remove(tranlocal, index);
    }

    @AtomicMethod
    public boolean remove(E item) {
        LinkedListTranlocal<E> tranlocal = ((LinkedListTranlocal<E>) AlphaStmUtils.load(this));
        return remove(tranlocal, item);
    }

    @AtomicMethod
    public E removeFirst() {
        LinkedListTranlocal<E> tranlocal = ((LinkedListTranlocal<E>) AlphaStmUtils.load(this));
        return removeFirst(tranlocal);
    }

    @AtomicMethod
    public E removeLast() {
        LinkedListTranlocal<E> tranlocal = ((LinkedListTranlocal<E>) AlphaStmUtils.load(this));
        return removeLast(tranlocal);
    }

    @AtomicMethod
    public E takeFirst() {
        LinkedListTranlocal<E> tranlocal = ((LinkedListTranlocal<E>) AlphaStmUtils.load(this));
        return takeFirst(tranlocal);
    }

    @AtomicMethod
    public E takeLast() {
        LinkedListTranlocal<E> tranlocal = ((LinkedListTranlocal<E>) AlphaStmUtils.load(this));
        return takeLast(tranlocal);
    }

    @AtomicMethod
    public void add(E item) {
        LinkedListTranlocal<E> tranlocal = ((LinkedListTranlocal<E>) AlphaStmUtils.load(this));
        add(tranlocal, item);
    }

    @AtomicMethod
    public void addInFront(E item) {
        LinkedListTranlocal<E> tranlocal = ((LinkedListTranlocal<E>) AlphaStmUtils.load(this));
        addInFront(tranlocal, item);
    }

    @AtomicMethod
    public int size() {
        LinkedListTranlocal<E> tranlocal = ((LinkedListTranlocal<E>) AlphaStmUtils.load(this));
        return size(tranlocal);
    }

    @AtomicMethod
    public boolean isEmpty() {
        LinkedListTranlocal<E> tranlocal = ((LinkedListTranlocal<E>) AlphaStmUtils.load(this));
        return isEmpty(tranlocal);
    }

    @AtomicMethod
    public String toString() {
        LinkedListTranlocal<E> tranlocal = ((LinkedListTranlocal<E>) AlphaStmUtils.load(this));
        return toString(tranlocal);
    }

    @Override
    public LinkedListTranlocal<E> privatize(long readVersion) {
        LinkedListTranlocal<E> origin = (LinkedListTranlocal<E>) load(readVersion);
        if (origin == null) {
            throw new LoadUncommittedException();
        }
        return new LinkedListTranlocal<E>(origin);
    }

    public void clear(LinkedListTranlocal<E> tranlocal) {
        if (tranlocal.committed) {
            throw new ReadonlyException();
        }

        tranlocal.head = null;
        tranlocal.tail = null;
        tranlocal.size = 0;
    }

    public int getFirstIndexOf(LinkedListTranlocal<E> tranlocal, E item) {
        LinkedNode<E> node = tranlocal.head;
        int index = 0;
        while (node != null) {
            if (node.getValue() == item) {
                return index;
            }
            index++;
            node = node.getNext();
        }

        return -1;
    }

    public void addInFront(LinkedListTranlocal<E> tranlocal, E item) {
        if (tranlocal.committed) {
            throw new ReadonlyException();
        }

        LinkedNode<E> node = new LinkedNode<E>(item);
        if (tranlocal.size == 0) {
            tranlocal.head = node;
            tranlocal.tail = node;
        } else {
            node.setNext(tranlocal.head);
            tranlocal.head.setPrevious(node);
            tranlocal.head = node;
        }

        tranlocal.size++;
    }

    public void add(LinkedListTranlocal<E> tranlocal, E item) {
        if (tranlocal.committed) {
            throw new ReadonlyException();
        }

        LinkedNode<E> node = new LinkedNode<E>(item);
        if (tranlocal.size == 0) {
            tranlocal.head = node;
            tranlocal.tail = node;
        } else {
            node.setPrevious(tranlocal.tail);
            tranlocal.tail.setNext(node);
            tranlocal.tail = node;
        }

        tranlocal.size++;
    }

    public int size(LinkedListTranlocal<E> tranlocal) {
        return tranlocal.size;
    }

    public boolean isEmpty(LinkedListTranlocal<E> tranlocal) {
        return tranlocal.size == 0;
    }

    public E removeFirst(LinkedListTranlocal<E> tranlocal) {
        if (tranlocal.committed) {
            throw new ReadonlyException();
        }

        if (tranlocal.size == 0) {
            throw new NoSuchElementException();
        }

        if (tranlocal.size == 1) {
            E result = tranlocal.head.getValue();
            tranlocal.head = null;
            tranlocal.tail = null;
            tranlocal.size = 0;
            return result;
        } else {
            LinkedNode<E> oldHead = tranlocal.head;
            LinkedNode<E> next = tranlocal.head.getNext();
            next.setPrevious(null);
            tranlocal.head = next;
            tranlocal.size--;
            return oldHead.getValue();
        }
    }

    public E removeLast(LinkedListTranlocal<E> tranlocal) {
        if (tranlocal.committed) {
            throw new ReadonlyException();
        }

        if (tranlocal.size == 0) {
            throw new NoSuchElementException();
        }

        if (tranlocal.size == 1) {
            E result = tranlocal.head.getValue();
            tranlocal.head = null;
            tranlocal.tail = null;
            tranlocal.size = 0;
            return result;
        } else {
            LinkedNode<E> oldTail = tranlocal.tail;
            LinkedNode<E> previous = tranlocal.tail.getPrevious();
            previous.setNext(null);
            tranlocal.tail = previous;
            tranlocal.size--;
            return oldTail.getValue();
        }
    }

    public E takeFirst(LinkedListTranlocal<E> tranlocal) {
        if (isEmpty(tranlocal)) {
            retry();
        }

        return removeFirst(tranlocal);
    }

    public E takeLast(LinkedListTranlocal<E> tranlocal) {
        if (isEmpty(tranlocal)) {
            retry();
        }

        return removeFirst(tranlocal);
    }

    public E remove(LinkedListTranlocal<E> tranlocal, int index) {
        if (tranlocal.committed) {
            throw new ReadonlyException();
        }

        if (index < 0 || index >= tranlocal.size) {
            throw new IllegalArgumentException();
        }

        throw new TodoException();
    }

    public boolean remove(LinkedListTranlocal<E> tranlocal, E item) {
        if (tranlocal.committed) {
            throw new ReadonlyException();
        }

        int indexOf = getFirstIndexOf(tranlocal, item);
        if (indexOf == -1) {
            return false;
        } else {
            remove(tranlocal, indexOf);
            return true;
        }
    }

    public E get(LinkedListTranlocal<E> tranlocal, int index) {
        if (index < 0 || index >= tranlocal.size) {
            throw new IllegalArgumentException();
        }

        //todo: if you are closer to the end, start searching from the end and not from the beginning.
        LinkedNode<E> result = tranlocal.head;
        for (int k = 0; k < index; k++) {
            result = result.getNext();
        }

        return result.getValue();
    }

    public Iterator<E> iterator(LinkedListTranlocal<E> tranlocal) {
        return new IteratorImpl<E>(tranlocal.head);
    }

    static class IteratorImpl<E> implements Iterator<E> {

        private final Ref<LinkedNode<E>> nodeRef = new Ref<LinkedNode<E>>();

        public IteratorImpl(LinkedNode<E> first) {
            nodeRef.set(first);
        }

        @Override
        @AtomicMethod
        public boolean hasNext() {
            return !nodeRef.isNull();
        }

        @Override
        @AtomicMethod
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            LinkedNode<E> old = nodeRef.get();
            nodeRef.set(old.getNext());
            return old.getValue();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public String toString(LinkedListTranlocal<E> tranlocal) {
        if (isEmpty()) {
            return "[]";
        }

        LinkedNode node = tranlocal.head;
        StringBuffer sb = new StringBuffer();
        sb.append('[');
        sb.append(node.getValue());
        node = node.getNext();
        while (node != null) {
            sb.append(',');
            sb.append(node.getValue());
            node = node.getNext();
        }
        sb.append(']');
        return sb.toString();
    }
}

final class LinkedListTranlocal<E> extends AlphaTranlocal {

    final LinkedList<E> atomicObject;
    int size;
    LinkedNode<E> head;
    LinkedNode<E> tail;
    LinkedListTranlocal<E> origin;

    public LinkedListTranlocal(LinkedListTranlocal<E> origin) {
        this.version = origin.version;
        this.atomicObject = origin.atomicObject;
        this.head = origin.head;
        this.tail = origin.tail;
        this.size = origin.size;
        this.origin = origin;
    }

    public LinkedListTranlocal(LinkedList<E> atomicObject) {
        this.version = Long.MIN_VALUE;
        this.atomicObject = atomicObject;
        this.size = 0;
        this.head = null;
        this.tail = null;
    }

    @Override
    public AlphaAtomicObject getAtomicObject() {
        return atomicObject;
    }


    @Override
    public AlphaTranlocalSnapshot takeSnapshot() {
        throw new TodoException();
    }

    @Override
    public void prepareForCommit(long writeVersion) {
        this.version = writeVersion;
        this.committed = true;
        this.origin = null;
    }

    @Override
    public DirtinessStatus getDirtinessStatus() {
        if (committed) {
            return DirtinessStatus.committed;
        } else if (origin == null) {
            return DirtinessStatus.fresh;
        } else if (origin.head != this.head) {
            return DirtinessStatus.dirty;
        } else if (origin.tail != this.tail) {
            return DirtinessStatus.dirty;
        } else if (origin.size != this.size) {
            return DirtinessStatus.dirty;
        } else {
            return DirtinessStatus.clean;
        }
    }
}

final class LinkedNode<E> extends FastAtomicObjectMixin {

    LinkedNode(final E value) {
        new AtomicTemplate() {
            @Override
            public Object execute(Transaction t) throws Exception {
                ((AlphaTransaction) t).attachNew(new LinkedNodeTranlocal(LinkedNode.this, value));
                return null;
            }
        }.execute();
    }

    @AtomicMethod
    public void setPrevious(LinkedNode<E> prev) {
        Transaction t = getThreadLocalTransaction();
        LinkedNodeTranlocal<E> tranlocal = (LinkedNodeTranlocal<E>) ((AlphaTransaction) t).load(this);
        setPrevious(tranlocal, prev);
    }

    @AtomicMethod
    public void setNext(LinkedNode<E> next) {
        Transaction t = getThreadLocalTransaction();
        LinkedNodeTranlocal<E> tranlocal = (LinkedNodeTranlocal<E>) ((AlphaTransaction) t).load(this);
        setNext(tranlocal, next);
    }

    @AtomicMethod
    public LinkedNode<E> getNext() {
        Transaction t = getThreadLocalTransaction();
        LinkedNodeTranlocal<E> tranlocal = (LinkedNodeTranlocal<E>) ((AlphaTransaction) t).load(this);
        return getNext(tranlocal);
    }

    @AtomicMethod
    public LinkedNode<E> getPrevious() {
        Transaction t = getThreadLocalTransaction();
        LinkedNodeTranlocal<E> tranlocal = (LinkedNodeTranlocal<E>) ((AlphaTransaction) t).load(this);
        return getPrevious(tranlocal);
    }

    @AtomicMethod
    public E getValue() {
        Transaction t = getThreadLocalTransaction();
        LinkedNodeTranlocal<E> tranlocal = (LinkedNodeTranlocal<E>) ((AlphaTransaction) t).load(this);
        return getValue(tranlocal);
    }

    @Override
    public LinkedNodeTranlocal<E> privatize(long readVersion) {
        LinkedNodeTranlocal<E> original = (LinkedNodeTranlocal<E>) load(readVersion);
        return new LinkedNodeTranlocal<E>(original);
    }

    public E getValue(LinkedNodeTranlocal<E> tranlocal) {
        return tranlocal.value;
    }

    public void setNext(LinkedNodeTranlocal<E> tranlocal, LinkedNode<E> next) {
        if (tranlocal.committed) {
            throw new ReadonlyException();
        }

        tranlocal.next = next;
    }

    public LinkedNode<E> getNext(LinkedNodeTranlocal<E> tranlocal) {
        return tranlocal.next;
    }

    public LinkedNode<E> getPrevious(LinkedNodeTranlocal<E> tranlocal) {
        return tranlocal.prev;
    }

    public void setPrevious(LinkedNodeTranlocal<E> tranlocal, LinkedNode<E> prev) {
        if (tranlocal.committed) {
            throw new ReadonlyException();
        }

        tranlocal.prev = prev;
    }
}

final class LinkedNodeTranlocal<E> extends AlphaTranlocal {
    final LinkedNode<E> atomicObject;
    LinkedNodeTranlocal<E> origin;
    LinkedNode<E> next;
    LinkedNode<E> prev;
    final E value;

    public LinkedNodeTranlocal(LinkedNode<E> atomicObject, E value) {
        this.atomicObject = atomicObject;
        this.value = value;
        this.next = null;
        this.prev = null;
        this.version = Long.MIN_VALUE;
    }

    public LinkedNodeTranlocal(LinkedNodeTranlocal<E> origin) {
        this.atomicObject = origin.atomicObject;
        this.version = origin.version;
        this.value = origin.value;
        this.next = origin.next;
        this.prev = origin.prev;
        this.origin = origin;
    }

    @Override
    public AlphaAtomicObject getAtomicObject() {
        return atomicObject;
    }

    @Override
    public void prepareForCommit(long writeVersion) {
        this.version = writeVersion;
        this.committed = true;
        this.origin = null;
    }

    @Override
    public AlphaTranlocalSnapshot takeSnapshot() {
        throw new TodoException();
    }

    public DirtinessStatus getDirtinessStatus() {
        if (committed) {
            return DirtinessStatus.committed;
        } else if (origin == null) {
            return DirtinessStatus.fresh;
        } else if (origin.next != this.next) {
            return DirtinessStatus.dirty;
        } else if (origin.prev != this.prev) {
            return DirtinessStatus.dirty;
        } else {
            return DirtinessStatus.clean;
        }
    }
}