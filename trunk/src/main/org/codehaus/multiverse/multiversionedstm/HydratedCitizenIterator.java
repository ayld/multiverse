package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.util.IdentityHashSet;
import org.codehaus.multiverse.util.ArrayIterator;
import org.codehaus.multiverse.util.CompositeIterator;

import java.util.*;

public final class HydratedCitizenIterator implements Iterator<Citizen> {

    private final IdentityHashSet<Citizen> touchedSet = new IdentityHashSet();
    private final IdentityHashSet<Citizen> iterateSet = new IdentityHashSet();
    private Iterator<Citizen> iterator;
    private Citizen nextCitizen;

    public HydratedCitizenIterator(Iterator<Citizen>... rootIterators) {
        if (rootIterators == null) throw new NullPointerException();
        iterator = new CompositeIterator<Citizen>(rootIterators);
    }

    public HydratedCitizenIterator(Citizen... roots) {
        iterator = new ArrayIterator<Citizen>(roots);
    }

    public boolean hasNext() {
        if (nextCitizen != null)
            return true;

        do {
            if (findNextInCurrentIterator())
                return true;
        } while (findNextIterator());

        return false;
    }

    private boolean findNextIterator() {
        if (iterateSet.isEmpty())
            return false;

        iterator = removeRandomItemFromIteratorSet().___directReachableIterator();
        return true;
    }

    private Citizen removeRandomItemFromIteratorSet() {
        Iterator<Citizen> it = iterateSet.iterator();
        Citizen citizen = it.next();
        it.remove();
        return citizen;
    }

    private boolean findNextInCurrentIterator() {
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

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
