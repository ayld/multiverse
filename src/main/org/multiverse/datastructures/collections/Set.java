package org.multiverse.datastructures.collections;

import static org.multiverse.api.StmUtils.retry;
import org.multiverse.api.annotations.AtomicObject;

import java.util.Iterator;

/**
 * @author Peter Veentjer
 */
@AtomicObject
public class Set<E extends Comparable> {

    private final static Object VALUE = new Object();

    private final BalancedTree<E, Object> tree = new BalancedTree<E, Object>();

    private final int maxCapacity;

    public Set() {
        this.maxCapacity = Integer.MAX_VALUE;
    }

    public Set(int maxCapacity) {
        if (maxCapacity < 0) {
            throw new IllegalArgumentException();
        }
        this.maxCapacity = maxCapacity;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public boolean add(E item) {
        if (size() == maxCapacity) {
            retry();
        }

        return tree.put(item, VALUE) == null;
    }

    public boolean remove(E item) {
        return tree.remove(item) != null;
    }

    public void awaitItemToComeAvailable(E item) {
        tree.await(item);
    }

    public boolean contains(E item) {
        return tree.contains(item);
    }

    public boolean isEmpty() {
        return tree.isEmpty();
    }

    public int size() {
        return tree.size();
    }

    public void clear() {
        tree.clear();
    }

    public Iterator<E> iterator() {
        return tree.keys();
    }

    public String toString() {
        if (isEmpty()) {
            return "[]";
        }

        Iterator<E> it = iterator();
        StringBuffer sb = new StringBuffer();
        sb.append('[');
        sb.append(it.next());

        for (; it.hasNext();) {
            sb.append(',');
            sb.append(it.next());
        }

        sb.append(']');
        return sb.toString();
    }
}
