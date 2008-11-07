package org.codehaus.multiverse.multiversionedstm;

import org.codehaus.multiverse.util.IdentityHashSet;
import org.codehaus.multiverse.util.ArrayIterator;

import java.util.*;

public class HydratedCitizenIterator implements Iterator<Citizen> {

    private final IdentityHashSet<Citizen> touched = new IdentityHashSet();
    private final IdentityHashSet<Citizen> iterateSet = new IdentityHashSet();
    private Iterator<Citizen> iterator;
    private Citizen nextCitizen;

    public HydratedCitizenIterator(Iterator<Citizen> rootIterator) {
        if (rootIterator == null) throw new NullPointerException();
        iterator = rootIterator;
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

        Citizen citizen = removeItemFromIteratorSet();

        iterator = citizen.___directReachableIterator();
        return true;
    }

    private Citizen removeItemFromIteratorSet() {
        Iterator<Citizen> it = iterateSet.iterator();
        Citizen citizen = it.next();
        it.remove();
        return citizen;
    }

    private boolean findNextInCurrentIterator() {
        while (iterator.hasNext()) {
            Citizen citizen = iterator.next();
            if (touched.add(citizen)) {
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
