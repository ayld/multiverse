package org.codehaus.multiverse.multiversionedstm.growingheap.heapnodes;

import org.codehaus.multiverse.multiversionedstm.DehydratedStmObject;

public interface HeapNode {

    long getHandle();

    long getVersion();

    DehydratedStmObject getContent();

    HeapNode createNew(DehydratedStmObject change, long changeVersion);

    HeapNode find(long handle);
}
