package org.codehaus.multiverse.util;

import java.util.AbstractSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * A {@link Set} that works based on object identity. Just like the {@link IdentityHashMap}. It even
 * uses a IdentifyHashMap as a map.
 * <p/>
 * This class is not threadsafe.
 * <p/>
 * todo: what about equals/hash of set itself?
 * todo: performance sucks because of the System.identityHashCode being used.
 *
 * @author Peter Veentjer.
 */
public final class IdentityHashSet<E> extends AbstractSet<E> implements Cloneable {

    // Dummy value to associate with an Object in the backing Map
    private static final Object PRESENT = new Object();

    private final IdentityHashMap<E, Object> map;

    public IdentityHashSet() {
        this(new IdentityHashMap<E, Object>());
    }

    public IdentityHashSet(IdentityHashMap<E, Object> map) {
        if (map == null) throw new NullPointerException();
        this.map = map;
    }

    public Iterator<E> iterator() {
        return map.keySet().iterator();
    }

    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    @Override
    public boolean add(E e) {
        return map.put(e, PRESENT) == null;
    }

    @Override
    public boolean remove(Object o) {
        return map.remove(o) == PRESENT;
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Object clone() {
        return new IdentityHashSet((IdentityHashMap) map.clone());
    }
}
