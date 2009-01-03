package org.codehaus.multiverse.multiversionedstm.utils;

import org.codehaus.multiverse.multiversionedstm.StmObject;
import org.codehaus.multiverse.util.iterators.ArrayIterator;
import org.codehaus.multiverse.util.iterators.CompositeIterator;

import java.util.*;

/**
 * An {@link Iterator} that iterates over {@link org.codehaus.multiverse.multiversionedstm.StmObject}, including their loaded members. Members are not
 * traversed if they are not loaded because we don't want to load very large object graphs. It uses the
 * {@link org.codehaus.multiverse.multiversionedstm.StmObject#___loadedMembers()} for iteration.
 * <p/>
 * This iterator gives the guarantee that each item is returned only once.
 * <p/>
 * This iterator is has protected against cycles, so for you don't need to worry about that.
 * <p/>
 * This iterator doesn't support removal.
 * <p/>
 * This iterator is not threadsafe.
 *
 * @author Peter Veentjer.
 */
public final class StmObjectIterator implements Iterator<StmObject> {

    //all StmObject that already are returned. This prevent multiple returns of the same item.
    private final Set<Long> touchedSet = new HashSet<Long>();
    //all StmObjects which members need to be traversed.
    private final Map<Long, StmObject> todoMembers = new HashMap<Long, StmObject>();

    private Iterator<StmObject> iterator;

    private StmObject next;

    public StmObjectIterator(Iterator<StmObject>... rootIterators) {
        if (rootIterators == null) throw new NullPointerException();
        iterator = new CompositeIterator<StmObject>(rootIterators);
    }

    public StmObjectIterator(StmObject... roots) {
        if (roots == null) throw new NullPointerException();
        iterator = new ArrayIterator<StmObject>(roots);
    }

    public boolean hasNext() {
        if (next != null)
            return true;

        do {
            if (findNextInCurrentIterator())
                return true;
        } while (findNextIterator());

        return false;
    }

    /**
     * Finds the next Iterator. If a new one is found, the iterator field is updated.
     *
     * @return true if one if found, false otherwise.
     */
    private boolean findNextIterator() {
        if (todoMembers.isEmpty())
            return false;

        iterator = takeItemFromTodoMembers().___loadedMembers();
        return true;
    }

    /**
     * Takes an item from the todoMembers
     *
     * @return the taken item.
     * @throws NoSuchElementException if the iteratorSet is empty.
     */
    private StmObject takeItemFromTodoMembers() {
        Iterator<StmObject> it = todoMembers.values().iterator();
        StmObject result = it.next();
        it.remove();
        return result;
    }

    /**
     * Finds the nextCitizen in the current iterator. If one if found, the nextCitizen field is updated
     * with the found value. If the found citizen is already touched (so already had its turn) it can't
     * be used, and the next element in the iterator has to be checked. If an untouched citizen is found,
     * the nextCitizen field is set, and it is added to the iterateSet.
     *
     * @return true if one if found, false otherwise.
     */
    private boolean findNextInCurrentIterator() {
        while (iterator.hasNext()) {
            StmObject object = iterator.next();
            //long handle = object.___getHandle();
            if (touchedSet.add(object.___getHandle())) {
                todoMembers.put(object.___getHandle(), object);
                next = object;
                return true;
            }
        }

        return false;
    }

    public StmObject next() {
        if (hasNext())
            return returnNext();

        throw new NoSuchElementException();
    }

    /**
     * Returns the next StmObject. This method requires that the next is set.
     *
     * @return the next StmObject. Value will always be no equal to null.
     */
    private StmObject returnNext() {
        assert next != null;
        StmObject result = next;
        next = null;
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @throws UnsupportedOperationException
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
