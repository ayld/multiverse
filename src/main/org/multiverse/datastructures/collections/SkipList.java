package org.multiverse.datastructures.collections;

import org.multiverse.api.annotations.AtomicObject;
import org.multiverse.utils.TodoException;

/**
 * A SkipList implementation:
 * <p/>
 * http://en.wikipedia.org/wiki/Skip_list
 *
 * @author Peter Veentjer.
 * @param <K>
 * @param <V>
 */
@AtomicObject
public class SkipList<K extends Comparable, V> {

    private int size;

    public V get(K key) {
        if (key == null) {
            throw new NullPointerException();
        }

        throw new TodoException();
    }

    public boolean contains(K key) {
        return get(key) != null;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public V put(K key, V newValue) {
        throw new TodoException();
    }

    public V remove(K key) {
        throw new TodoException();
    }

    public void clear() {
        size = 0;
    }
}
