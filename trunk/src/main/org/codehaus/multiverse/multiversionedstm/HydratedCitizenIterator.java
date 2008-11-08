package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.util.IdentityHashSet;
import org.codehaus.multiverse.util.ArrayIterator;
import org.codehaus.multiverse.util.CompositeIterator;

import java.util.*;

/**
 * An {@link Iterator} that iterates over {@link Citizen} objects, including their references that
 * are loaded or set. References that are lazy loaded, are not traversed. It uses the
 * {@link org.codehaus.multiverse.multiversionedstm.Citizen#___directReachableIterator()} for iteration.
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
public final class HydratedCitizenIterator implements Iterator<Citizen> {

    //all citizens that already have been returned with the next method.
    private final IdentityHashSet<Citizen> touchedSet = new IdentityHashSet();
    //all citizen objects that need to be traversed
    private final IdentityHashSet<Citizen> iterateSet = new IdentityHashSet();
    private Iterator<Citizen> iterator;

    private Citizen nextCitizen;

    public HydratedCitizenIterator(Iterator<Citizen>... rootIterators) {
        if (rootIterators == null) throw new NullPointerException();
        iterator = new CompositeIterator<Citizen>(rootIterators);
    }

    public HydratedCitizenIterator(Citizen... roots) {
        if (roots == null) throw new NullPointerException();
        iterator = new ArrayIterator<Citizen>(roots);
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

        iterator = removeRandomItemFromIteratorSet().___directReachableIterator();
        return true;
    }

    /**
     * Removes a random element from the iterator set.
     *
     * @return the
     * @throws NoSuchElementException if the iteratorSet is empty.
     */
    private Citizen removeRandomItemFromIteratorSet() {
        Iterator<Citizen> it = iterateSet.iterator();
        Citizen citizen = it.next();
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
            Citizen citizen = iterator.next();
            if (touchedSet.add(citizen)) {
                iterateSet.add(citizen);
                nextCitizen = citizen;
                return true;
            }
        }

        return false;
    }

    public Citizen next() {
        if (nextCitizen != null || hasNext())
            return returnNextCitizen();

        throw new NoSuchElementException();
    }

    private Citizen returnNextCitizen() {
        Citizen result = nextCitizen;
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
