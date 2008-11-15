package org.codehaus.multiverse.util;

import java.util.*;

/**
 * A {@link Set} that works based on object identity. Just like the {@link IdentityHashMap}. It even
 * uses a IdentifyHashMap as a map.
 *
 * This class is not threadsafe.
 *
 * todo: what about equals/hash of set itself?
 *
 * @author Peter Veentjer.
 */
public final class IdentityHashSet<E> extends AbstractSet<E> {

    // Dummy value to associate with an Object in the backing Map
    private static final Object PRESENT = new Object();

    private final IdentityHashMap<E, Object> map;

    public IdentityHashSet() {
        this(new IdentityHashMap<E, Object>());
    }

    public IdentityHashSet(IdentityHashMap map) {
        if (map == null) throw new NullPointerException();
        this.map = map;
    }

    public Iterator<E> iterator() {
        return map.keySet().iterator();
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    public boolean add(E e) {
        return map.put(e, PRESENT) == null;
    }

    public boolean remove(Object o) {
        return map.remove(o) == PRESENT;
    }

    public void clear() {
        map.clear();
    }

    public Object clone() {
        return new IdentityHashSet((IdentityHashMap) map.clone());
    }
}
