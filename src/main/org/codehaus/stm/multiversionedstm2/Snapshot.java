package org.codehaus.stm.multiversionedstm2;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * This object should be completely immutable.
 */
public class Snapshot {
    public final long version;
    public final Map<Long, DehydratedCitizen> roots;
    public final IdentityHashMap<DehydratedCitizen, Long> invertedRoots = new IdentityHashMap<DehydratedCitizen, Long>();

    public Snapshot() {
        this(0, new HashMap<Long, DehydratedCitizen>());
    }

    public Snapshot(long version, Map<Long, DehydratedCitizen> roots) {
        this.version = version;
        this.roots = roots;
        for(Map.Entry<Long, DehydratedCitizen> entry: roots.entrySet()){
            invertedRoots.put(entry.getValue(), entry.getKey());
        }
    }
}
