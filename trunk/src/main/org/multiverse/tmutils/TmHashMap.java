package org.multiverse.tmutils;

import java.util.AbstractMap;
import java.util.Set;

public class TmHashMap<K, V> extends AbstractMap<K, V> {

    //private MultiverseLinkedList< >

    @Override
    public V get(Object key) {
        throw new RuntimeException();
    }

    @Override
    public V put(K key, V value) {
        throw new RuntimeException();
    }

    @Override
    public Set entrySet() {
        throw new RuntimeException();
    }
}
