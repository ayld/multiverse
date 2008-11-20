package org.codehaus.multiverse.multiversionedstm2;

import org.codehaus.multiverse.transaction.ObjectDoesNotExistException;

import java.util.Map;
import java.util.Set;


public class TheMap {

    private final TheMap parent;
    private final Map<Long, DehydratedCitizen> changes;
    private final Set<Long> deletes;

    public TheMap(TheMap parent, Map<Long, DehydratedCitizen> changes, Set<Long> deletes) {
        this.parent = parent;
        this.changes = changes;
        this.deletes = deletes;
    }

    public DehydratedCitizen get(long key) {
        DehydratedCitizen local = changes.get(key);
        if (local != null)
            return local;

        if (deletes.contains(key))
            throw new ObjectDoesNotExistException();

        if (parent == null)
            throw new ObjectDoesNotExistException();

        return parent.get(key);
    }

    public void merge(){
        
    }
}
