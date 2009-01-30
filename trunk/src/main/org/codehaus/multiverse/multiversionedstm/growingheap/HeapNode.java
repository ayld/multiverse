package org.codehaus.multiverse.multiversionedstm.growingheap;

import org.codehaus.multiverse.multiversionedstm.DehydratedStmObject;

public interface HeapNode {

    long getHandle();

    DehydratedStmObject getContent();

    HeapNode createNew(DehydratedStmObject change);

    HeapNode find(long handle);
}
