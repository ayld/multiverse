package org.multiverse.stms.alpha.manualinstrumentation;

import static org.multiverse.api.StmUtils.retry;
import org.multiverse.api.Transaction;
import org.multiverse.api.annotations.AtomicMethod;
import org.multiverse.api.exceptions.LoadUncommittedAtomicObjectException;
import org.multiverse.api.exceptions.ReadonlyException;
import org.multiverse.stms.alpha.AlphaStmUtils;
import org.multiverse.stms.alpha.DirtinessStatus;
import org.multiverse.stms.alpha.Tranlocal;
import org.multiverse.stms.alpha.TranlocalSnapshot;
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
                t.attachNew(new LinkedListTranlocal(LinkedList.this));
                return null;
            }
        }.execute();
    }

    @AtomicMethod
    public Iterator<E> iterator() {
        return ((LinkedListTranlocal<E>) AlphaStmUtils.privatize(this)).iterator();
    }

    @AtomicMethod
    public int getFirstIndexOf(E item) {
        return ((LinkedListTranlocal<E>) AlphaStmUtils.privatize(this)).firstIndexOf(item);
    }

    @AtomicMethod
    public E get(int index) {
        return ((LinkedListTranlocal<E>) AlphaStmUtils.privatize(this)).get(index);
    }

    @AtomicMethod
    public void clear() {
        ((LinkedListTranlocal<E>) AlphaStmUtils.privatize(this)).clear();
    }

    @AtomicMethod
    public E remove(int index) {
        return ((LinkedListTranlocal<E>) AlphaStmUtils.privatize(this)).remove(index);
    }

    @AtomicMethod
    public boolean remove(E item) {
        return ((LinkedListTranlocal<E>) AlphaStmUtils.privatize(this)).remove(item);
    }

    @AtomicMethod
    public E removeFirst() {
        return ((LinkedListTranlocal<E>) AlphaStmUtils.privatize(this)).removeFirst();
    }

    @AtomicMethod
    public E removeLast() {
        return ((LinkedListTranlocal<E>) AlphaStmUtils.privatize(this)).removeLast();
    }

    @AtomicMethod
    public E takeFirst() {
        return ((LinkedListTranlocal<E>) AlphaStmUtils.privatize(this)).takeFirst();
    }

    @AtomicMethod
    public E takeLast() {
        return ((LinkedListTranlocal<E>) AlphaStmUtils.privatize(this)).takeLast();
    }

    @AtomicMethod
    public void add(E item) {
        ((LinkedListTranlocal<E>) AlphaStmUtils.privatize(this)).add(item);
    }

    @AtomicMethod
    public void addInFront(E item) {
        ((LinkedListTranlocal<E>) AlphaStmUtils.privatize(this)).addInFront(item);
    }

    @AtomicMethod
    public int size() {
        return ((LinkedListTranlocal<E>) AlphaStmUtils.privatize(this)).size();
    }

    @AtomicMethod
    public boolean isEmpty() {
        return ((LinkedListTranlocal<E>) AlphaStmUtils.privatize(this)).isEmpty();
    }

    @AtomicMethod
    public String toString() {
        return ((LinkedListTranlocal<E>) AlphaStmUtils.privatize(this)).toString();
    }

    @Override
    public LinkedListTranlocal<E> privatize(long readVersion) {
        LinkedListTranlocal<E> origin = (LinkedListTranlocal<E>) load(readVersion);
        if (origin == null) {
            throw new LoadUncommittedAtomicObjectException();
        }
        return new LinkedListTranlocal<E>(origin);
    }
}

final class LinkedListTranlocal<E> extends Tranlocal {

    private final LinkedList<E> atomicObject;
    private int size;
    private LinkedNode<E> head;
    private LinkedNode<E> tail;
    private LinkedListTranlocal<E> origin;

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
    public Object getAtomicObject() {
        return atomicObject;
    }

    public void clear() {
        if (committed) {
            throw new ReadonlyException();
        }

        head = null;
        tail = null;
        size = 0;
    }

    public int firstIndexOf(E item) {
        LinkedNode<E> node = head;
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

    public void addInFront(E item) {
        if (committed) {
            throw new ReadonlyException();
        }

        LinkedNode<E> node = new LinkedNode<E>(item);
        if (size == 0) {
            head = node;
            tail = node;
        } else {
            node.setNext(head);
            head.setPrevious(node);
            head = node;
        }

        size++;
    }

    public void add(E item) {
        if (committed) {
            throw new ReadonlyException();
        }

        LinkedNode<E> node = new LinkedNode<E>(item);
        if (size == 0) {
            head = node;
            tail = node;
        } else {
            node.setPrevious(tail);
            tail.setNext(node);
            tail = node;
        }

        size++;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public E removeFirst() {
        if (committed) {
            throw new ReadonlyException();
        }


        if (size == 0) {
            throw new NoSuchElementException();
        }

        if (size == 1) {
            E result = head.getValue();
            head = null;
            tail = null;
            size = 0;
            return result;
        } else {
            LinkedNode<E> oldHead = head;
            LinkedNode<E> next = head.getNext();
            next.setPrevious(null);
            head = next;
            size--;
            return oldHead.getValue();
        }
    }

    public E removeLast() {
        if (committed) {
            throw new ReadonlyException();
        }

        if (size == 0) {
            throw new NoSuchElementException();
        }

        if (size == 1) {
            E result = head.getValue();
            head = null;
            tail = null;
            size = 0;
            return result;
        } else {
            LinkedNode<E> oldTail = tail;
            LinkedNode<E> previous = tail.getPrevious();
            previous.setNext(null);
            tail = previous;
            size--;
            return oldTail.getValue();
        }
    }

    public E takeFirst() {
        if (isEmpty()) {
            retry();
        }

        return removeFirst();
    }

    public E takeLast() {
        if (isEmpty()) {
            retry();
        }

        return removeFirst();
    }

    public E remove(int index) {
        if (committed) {
            throw new ReadonlyException();
        }

        if (index < 0 || index >= size) {
            throw new IllegalArgumentException();
        }

        throw new TodoException();
    }

    public boolean remove(E item) {
        if (committed) {
            throw new ReadonlyException();
        }

        int indexOf = firstIndexOf(item);
        if (indexOf == -1) {
            return false;
        } else {
            remove(indexOf);
            return true;
        }
    }

    public E get(int index) {
        if (index < 0 || index >= size) {
            throw new IllegalArgumentException();
        }

        //todo: if you are closer to the end, start searching from the end and not from the beginning.
        LinkedNode<E> result = head;
        for (int k = 0; k < index; k++) {
            result = result.getNext();
        }

        return result.getValue();
    }

    public Iterator<E> iterator() {
        return new IteratorImpl<E>(head);
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

    @Override
    public String toString() {
        if (isEmpty()) {
            return "[]";
        }

        LinkedNode node = head;
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

    @Override
    public TranlocalSnapshot takeSnapshot() {
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
                t.attachNew(new LinkedNodeTranlocal(LinkedNode.this, value));
                return null;
            }
        }.execute();
    }

    @AtomicMethod
    public void setPrevious(LinkedNode<E> prev) {
        Transaction t = getThreadLocalTransaction();
        LinkedNodeTranlocal<E> tranlocalThis = (LinkedNodeTranlocal<E>) t.privatize(this);
        tranlocalThis.setPrevious(prev);
    }

    @AtomicMethod
    public void setNext(LinkedNode<E> next) {
        Transaction t = getThreadLocalTransaction();
        LinkedNodeTranlocal<E> tranlocalThis = (LinkedNodeTranlocal<E>) t.privatize(this);
        tranlocalThis.setNext(next);
    }

    @AtomicMethod
    public LinkedNode<E> getNext() {
        Transaction t = getThreadLocalTransaction();
        LinkedNodeTranlocal<E> tranlocalThis = (LinkedNodeTranlocal<E>) t.privatize(this);
        return tranlocalThis.getNext();
    }

    @AtomicMethod
    public LinkedNode<E> getPrevious() {
        Transaction t = getThreadLocalTransaction();
        LinkedNodeTranlocal<E> tranlocalThis = (LinkedNodeTranlocal<E>) t.privatize(this);
        return tranlocalThis.getPrevious();
    }

    @AtomicMethod
    public E getValue() {
        Transaction t = getThreadLocalTransaction();
        LinkedNodeTranlocal<E> tranlocalThis = (LinkedNodeTranlocal<E>) t.privatize(this);
        return tranlocalThis.getValue();
    }

    @Override
    public LinkedNodeTranlocal<E> privatize(long readVersion) {
        LinkedNodeTranlocal<E> original = (LinkedNodeTranlocal<E>) load(readVersion);
        return new LinkedNodeTranlocal<E>(original);
    }
}

final class LinkedNodeTranlocal<E> extends Tranlocal {
    private final LinkedNode<E> atomicObject;
    private LinkedNodeTranlocal<E> origin;
    private LinkedNode<E> next;
    private LinkedNode<E> prev;
    private final E value;

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
    public Object getAtomicObject() {
        return atomicObject;
    }

    public E getValue() {
        return value;
    }

    public void setNext(LinkedNode<E> next) {
        if (committed) {
            throw new ReadonlyException();
        }

        this.next = next;
    }

    public LinkedNode<E> getNext() {
        return next;
    }

    public LinkedNode<E> getPrevious() {
        return prev;
    }

    public void setPrevious(LinkedNode<E> prev) {
        if (committed) {
            throw new ReadonlyException();
        }

        this.prev = prev;
    }

    @Override
    public void prepareForCommit(long writeVersion) {
        this.version = writeVersion;
        this.committed = true;
        this.origin = null;
    }

    @Override
    public TranlocalSnapshot takeSnapshot() {
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