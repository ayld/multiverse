package org.multiverse.multiversionedstm.examples;

import org.multiverse.api.LazyReference;
import org.multiverse.api.Transaction;
import org.multiverse.multiversionedstm.*;

import java.util.AbstractSequentialList;
import java.util.Collection;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * An STM version of the {@link java.util.LinkedList}.
 * <p/>
 * todo: linkedlist should also implement Deque.
 *
 * @author Peter Veentjer.
 * @param <E>
 */
public final class ExampleLinkedList<E> extends AbstractSequentialList<E> implements MaterializedObject {

    private Entry<E> header;
    private int size;

    public ExampleLinkedList() {
        size = 0;
        header = new Entry<E>(null, null, null);
        header.next = header.previous = header;
        handle = new DefaultMultiversionedHandle<ExampleLinkedList<E>>();
    }

    public ExampleLinkedList(Collection<? extends E> c) {
        this();
        addAll(c);
    }

    @Override
    public E get(int index) {
        //todo: hier zal ook nog een check gedaan moeten worden.
        return entry(index).element;
    }

    public E getFirst() {
        if (size == 0)
            throw new NoSuchElementException();

        //--- generated ------------
        if (header.nextRef != null) {
            header.next = header.nextRef.get();
            header.nextRef = null;
        }
        //------ end generated --------

        return header.next.element;
    }

    public E getLast() {
        if (size == 0)
            throw new NoSuchElementException();

        //---- generated ------
        if (header.previousRef != null) {
            header.previous = header.previousRef.get();
            header.previousRef = null;
        }
        //----- end generated ---

        return header.previous.element;
    }

    public E removeFirst() {
        // ----- generated ------
        if (header.nextRef != null) {
            header.next = header.nextRef.get();
            header.nextRef = null;
        }
        // ---- end generated ----
        return remove(header.next);
    }

    public E removeLast() {
        //----- generated ------
        if (header.previousRef != null) {
            header.previous = header.previousRef.get();
            header.previousRef = null;
        }
        //---- end generated ----

        return remove(header.previous);
    }


    private E remove(Entry<E> e) {
        if (e == header)
            throw new NoSuchElementException();

        E result = e.element;
        //---- generated ----------
        if (e.nextRef != null) {
            e.next = e.nextRef.get();
            e.nextRef = null;
        }

        if (e.previousRef != null) {
            e.previous = e.previousRef.get();
            e.previousRef = null;
        }
        //---- end generated ------
        e.previous.next = e.next;

        //todo
        e.next.previous = e.previous;
        e.next = e.previous = null;

        e.element = null;
        size--;
        modCount++;
        return result;
    }

    @Override
    public boolean add(E e) {
        addBefore(e, header);
        return true;
    }

    private Entry<E> addBefore(E e, Entry<E> entry) {
        // ------- generated -------------
        if (entry.previousRef != null) {
            entry.previous = entry.previousRef.get();
            entry.previousRef = null;
        }
        // -------- end generated --------

        Entry<E> newEntry = new Entry<E>(e, entry, entry.previous);

        newEntry.previous.next = newEntry;

        //----- generated -----------
        if (entry.nextRef != null) {
            entry.next = entry.nextRef.get();
            entry.next = null;
        }
        //---- end generated ---------

        newEntry.next.previous = newEntry;

        size++;
        modCount++;
        return newEntry;
    }

    private Entry<E> entry(int index) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException("Index: " + index +
                    ", Size: " + size);

        Entry<E> e = header;

        if (index < (size >> 1)) {
            for (int i = 0; i <= index; i++) {
                //----- generated -----------
                if (e.nextRef != null) {
                    e.next = e.nextRef.get();
                    e.nextRef = null;
                }
                //-------- end generated -----

                e = e.next;
            }
        } else {
            for (int i = size; i > index; i--) {
                //------ generated --------------
                if (e.previousRef != null) {
                    e.previous = e.previousRef.get();
                    e.previousRef = null;
                }
                //------ end generated -----------

                e = e.previous;
            }
        }

        return e;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        throw new RuntimeException();
    }

    // ============== generated ===================

    private DematerializedLinkedList<E> lastDematerialized;
    private final MultiversionedHandle<ExampleLinkedList<E>> handle;

    private ExampleLinkedList(DematerializedLinkedList<E> dematerializedLinkedList, Transaction t) {
        this.handle = dematerializedLinkedList.handle;
        this.lastDematerialized = dematerializedLinkedList;
        this.size = dematerializedLinkedList.size;
        this.header = t.readSelfManaged(dematerializedLinkedList.header);
    }

    @Override
    public MultiversionedHandle getHandle() {
        return handle;
    }

    @Override
    public boolean isDirty() {
        if (lastDematerialized == null)
            return true;

        if (lastDematerialized.header != MultiversionedStmUtils.getHandle(header))
            return true;

        return false;
    }

    @Override
    public DematerializedLinkedList dematerialize() {
        return lastDematerialized = new DematerializedLinkedList<E>(this);
    }

    @Override
    public void walkMaterializedMembers(MemberWalker memberWalker) {
        header.walkMaterializedMembers(memberWalker);
    }

    private MaterializedObject nextInChain;

    @Override
    public MaterializedObject getNextInChain() {
        return nextInChain;
    }

    @Override
    public void setNextInChain(MaterializedObject next) {
        this.nextInChain = next;
    }

    static class Entry<E> implements MaterializedObject {
        E element;
        Entry<E> next;
        Entry<E> previous;

        Entry(E element, Entry<E> next, Entry<E> previous) {
            this.element = element;
            this.next = next;
            this.previous = previous;
            this.handle = new DefaultMultiversionedHandle<Entry<E>>();
        }

        // ============== generated =======================

        LazyReference<Entry<E>> nextRef;
        LazyReference<Entry<E>> previousRef;
        DematerializedEntry<E> lastDematerialized;
        MultiversionedHandle<Entry<E>> handle;

        Entry(DematerializedEntry<E> dematerializedEntry, Transaction t) {
            this.handle = dematerializedEntry.handle;
            this.lastDematerialized = dematerializedEntry;
            this.nextRef = t.readLazyAndSelfManaged(dematerializedEntry.next);
            this.previousRef = t.readLazyAndSelfManaged(dematerializedEntry.previous);
            this.element = dematerializedEntry.element;
            //todo: element is not checked for stm membership
        }


        @Override
        public MultiversionedHandle getHandle() {
            return handle;
        }

        @Override
        public boolean isDirty() {
            if (lastDematerialized == null)
                return true;

            if (lastDematerialized.next != MultiversionedStmUtils.getHandle(nextRef, next))
                return true;

            if (lastDematerialized.previous != MultiversionedStmUtils.getHandle(previousRef, previous))
                return true;

            //todo: element

            return false;
        }

        @Override
        public DematerializedObject dematerialize() {
            return lastDematerialized = new DematerializedEntry<E>(this);
        }

        @Override
        public void walkMaterializedMembers(MemberWalker memberWalker) {
            if (next != null) memberWalker.onMember(next);
            if (previous != null) memberWalker.onMember(previous);
            //todo: value
        }

        private MaterializedObject nextInChain;

        @Override
        public MaterializedObject getNextInChain() {
            return nextInChain;
        }

        @Override
        public void setNextInChain(MaterializedObject next) {
            this.nextInChain = next;
        }
    }

    static class DematerializedLinkedList<E> implements DematerializedObject {
        private final MultiversionedHandle<ExampleLinkedList<E>> handle;
        private final int size;
        private MultiversionedHandle<Entry<E>> header;

        public DematerializedLinkedList(ExampleLinkedList<E> list) {
            this.handle = list.handle;
            this.size = list.size;
            this.header = MultiversionedStmUtils.getHandle(list.header);
        }

        @Override
        public MultiversionedHandle<ExampleLinkedList<E>> getHandle() {
            return handle;
        }

        @Override
        public ExampleLinkedList<E> rematerialize(Transaction t) {
            return new ExampleLinkedList<E>(this, t);
        }
    }

    static class DematerializedEntry<E> implements DematerializedObject {
        private final MultiversionedHandle<Entry<E>> handle;
        private final MultiversionedHandle<Entry<E>> next;
        private final MultiversionedHandle<Entry<E>> previous;
        private final E element;

        DematerializedEntry(Entry<E> entry) {
            this.handle = entry.handle;
            this.next = MultiversionedStmUtils.getHandle(entry.nextRef, entry.next);
            this.previous = MultiversionedStmUtils.getHandle(entry.previousRef, entry.previous);
            this.element = entry.element;
            //todo: ELEMENT
        }

        @Override
        public MultiversionedHandle getHandle() {
            return handle;
        }

        @Override
        public Entry<E> rematerialize(Transaction t) {
            return new Entry<E>(this, t);
        }
    }
}
