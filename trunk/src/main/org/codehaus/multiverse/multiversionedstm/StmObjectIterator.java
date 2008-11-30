package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.util.IdentityHashSet;
import org.codehaus.multiverse.util.iterators.ArrayIterator;
import org.codehaus.multiverse.util.iterators.CompositeIterator;

import java.util.*;

/**
 * An {@link Iterator} that iterates over {@link StmObject}, including their references that
 * are loaded or set. References that are lazy loaded, are not traversed. It uses the
 * {@link StmObject#___directReferencedIterator()} for iteration.
 * <p/>
 * This iterator also gives the guarantee that each item is traversed only once.
 * <p/>
 * This iterator is protected against cycles, so nothing to worry about.
 * <p/>
 * This iterator doesn't support removal.
 * <p/>
 * This iterator is not threadsafe.
 *
 * @author Peter Veentjer.
 */
public final class StmObjectIterator implements Iterator<StmObject> {

    //all dehydratedObjects that already have been returned with the next method.
    private final IdentityHashSet<StmObject> touchedSet = new IdentityHashSet();
    //all citizen objects that need to be traversed
    private final IdentityHashSet<StmObject> iterateSet = new IdentityHashSet();
    private Iterator<StmObject> iterator;

    private StmObject nextCitizen;

    public StmObjectIterator(Iterator<StmObject>... rootIterators) {
        if (rootIterators == null) throw new NullPointerException();
        iterator = new CompositeIterator<StmObject>(rootIterators);
    }

    public StmObjectIterator(StmObject... roots) {
        if (roots == null) throw new NullPointerException();
        iterator = new ArrayIterator<StmObject>(roots);
    }

    public boolean hasNext() {
        if (nextCitizen != null)
            return true;

        do {
            if (findNextCitizenInCurrentIterator())
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
        if (iterateSet.isEmpty())
            return false;

        iterator = removeRandomItemFromIteratorSet().___directReferencedIterator();
        return true;
    }

    /**
     * Removes a random element from the iterator set.
     *
     * @return the
     * @throws NoSuchElementException if the iteratorSet is empty.
     */
    private StmObject removeRandomItemFromIteratorSet() {
        Iterator<StmObject> it = iterateSet.iterator();
        StmObject citizen = it.next();
        it.remove();
        return citizen;
    }

    /**
     * Finds the nextCitizen in the current iterator. If one if found, the nextCitizen field is updated
     * with the found value. If the found citizen is already touched (so already had its turn) it can't
     * be used, and the next element in the iterator has to be checked. If an untouched citizen is found,
     * the nextCitizen field is set, and it is added to the iterateSet.
     *
     * @return true if one if found, false otherwise.
     */
    private boolean findNextCitizenInCurrentIterator() {
        while (iterator.hasNext()) {
            StmObject citizen = iterator.next();
            if (touchedSet.add(citizen)) {
                iterateSet.add(citizen);
                nextCitizen = citizen;
                return true;
            }
        }

        return false;
    }

    public StmObject next() {
        if (nextCitizen != null || hasNext())
            return returnNextCitizen();

        throw new NoSuchElementException();
    }

    private StmObject returnNextCitizen() {
        StmObject result = nextCitizen;
        nextCitizen = null;
        return result;
    }

    /**
     * @throws UnsupportedOperationException
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
