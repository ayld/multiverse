package org.codehaus.multiverse.multiversionedstm2;

import java.util.Map;
import java.util.IdentityHashMap;
import java.util.HashMap;

public class Heap {

    private final HeapSnapshot heapSnapshot;
  
    public Heap(HeapSnapshot heapSnapshot){
        if(heapSnapshot == null)throw new NullPointerException();
        this.heapSnapshot = heapSnapshot;
    }

    public HeapSnapshot createSnapshot() {
        return new HeapSnapshot();
    }
}
