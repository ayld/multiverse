package org.multiverse.datastructures.collections;

import org.multiverse.api.annotations.AtomicMethod;
import org.multiverse.api.annotations.AtomicObject;
import org.multiverse.datastructures.refs.Ref;
import org.multiverse.utils.TodoException;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * An {@link java.util.List} implementation that uses an array to store the items underneath and
 * uses STM for concurrency control. The array has a fixed capacity, so it can't grow. It is the first
 * structure that is used to experiment with arrays being used in the STM. Atm an array of refs is
 * used, but using instrumentation in the future this could be a basic array enhanced by instrumentation.
 * <p/>
 * The behavior is similar to the {@link ArrayList} except that this structure is transactional
 * and has a fixed capacity. In the future this class will be replaced by a growing array. The
 * reason why this isn't supported isn't that growing/shrinking the array is hard, but because
 * read/write conflicts are not detected and because of this it could be that items that added
 * items get lost (they get added on the wrong array) when also a shrink/grow is happening. The
 * same goes for removals and updates. Adding read/write conflict detection instead of just
 * add write/write conflict detection is something that needs some thought because it could reduce
 * concurrency because of an increase in conflicts. It also could decrease performance within a
 * single transaction.
 * <p/>
 * <p>Memory consistency effects: As with other concurrent collections, actions in a thread prior to
 * placing an object into a {@code FixedLengthArrayList}
 * <a href="package-summary.html#MemoryVisibility"><i>happen-before</i></a> actions subsequent to the
 * access or removal of that element from the {@code FixedLengthArrayList} in another thread.
 *
 * @param <E>
 */
@AtomicObject
public class FixedLengthArrayList<E> extends AbstractCollection<E> implements List<E> {

    private final Ref<E>[] array;
    private int firstFreeIndex;

    public FixedLengthArrayList(int capacity) {
        array = new Ref[capacity];
        for (int k = 0; k < capacity; k++) {
            array[k] = new Ref<E>();
        }
    }

    @Override
    public Iterator<E> iterator() {
        throw new TodoException();
    }

    @Override
    @AtomicMethod(readonly = true)
    public int size() {
        return firstFreeIndex;
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        if (c == null) {
            throw new NullPointerException();
        }

        if (c.isEmpty()) {
            return false;
        }

        throw new TodoException();
    }

    @Override
    public boolean add(E e) {
        if (firstFreeIndex >= array.length) {
            return false;
        }

        array[firstFreeIndex].set(e);
        firstFreeIndex++;
        return true;
    }

    @Override
    public E get(int index) {
        validateIndex(index);
        return array[index].get();
    }

    private void validateIndex(int index) {
        if (index < 0 || index >= firstFreeIndex) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void clear() {
        for (int k = 0; k < firstFreeIndex; k++) {
            array[k].clear();
        }
        firstFreeIndex = 0;
    }

    @Override
    public E set(int index, E element) {
        validateIndex(index);
        E oldElement = array[index].get();
        array[index].set(element);
        return oldElement;
    }

    @Override
    public void add(int index, E element) {
        throw new TodoException();
    }

    @Override
    public E remove(int index) {
        throw new TodoException();
    }

    @Override
    @AtomicMethod(readonly = true)
    public int indexOf(Object o) {
        for (int k = 0; k < firstFreeIndex; k++) {
            if (equals(array[k].get(), o)) {
                return k;
            }
        }

        return -1;
    }

    @Override
    @AtomicMethod(readonly = true)
    public int lastIndexOf(Object o) {
        for (int k = firstFreeIndex - 1; k >= 0; k--) {
            if (equals(array[k].get(), o)) {
                return k;
            }
        }

        return -1;
    }

    private static <E> boolean equals(E element, Object o) {
        return element == null ? o == null : element.equals(o);
    }

    @Override
    public ListIterator<E> listIterator() {
        throw new TodoException();
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        throw new TodoException();
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        throw new TodoException();
    }
}
