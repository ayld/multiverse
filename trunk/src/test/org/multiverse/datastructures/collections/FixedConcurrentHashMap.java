package org.multiverse.datastructures.collections;

import org.multiverse.api.annotations.AtomicMethod;
import org.multiverse.api.annotations.AtomicObject;

import java.util.AbstractMap;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Peter Veentjer
 */
@AtomicObject
public class FixedConcurrentHashMap<K, V> extends AbstractMap<K, V> implements ConcurrentMap<K, V> {

    private final Object[] array = null;


    @Override
    @AtomicMethod(readonly = true)
    public Set<Entry<K, V>> entrySet() {
        return null;  //todo
    }

    @Override
    public V putIfAbsent(K key, V value) {
        return null;  //todo
    }

    @Override
    public boolean remove(Object key, Object value) {
        return false;  //todo
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return false;  //todo
    }

    @Override
    public V replace(K key, V value) {
        return null;  //todo
    }
}
